package com.conveyal.lodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

import au.com.bytecode.opencsv.CSVReader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;	
import com.vividsolutions.jts.geom.Polygon;


public class App 
{
	static Blocks blocks;
	static Attributes attributes;
	
    public static void main( String[] args ) throws IOException, FactoryException
    {
    	GeometryJSON g = new GeometryJSON();
    	
    	BufferedReader fs = new BufferedReader(new FileReader("data/boundary.json"));
    	Geometry boundary = g.read(fs);
    	Geometry envelope = boundary.getEnvelope();
    	System.out.println("Bounding box: " + envelope.getBoundary());
    	fs.close();
    	
    	blocks = new Blocks();
    	blocks.load(new File("data/tl_2013_34_tabblock.shp"), boundary);
    	blocks.load(new File("data/tl_2013_36_tabblock.shp"), boundary);
    	blocks.load(new File("data/tl_2013_09_tabblock.shp"), boundary);
    	
    	attributes = new Attributes(new File("data/attribute_groups.csv"));
    	attributes.load(new File("data/ny_rac_S000_JT00_2011.csv"));
    	attributes.load(new File("data/nj_rac_S000_JT00_2011.csv"));
    	attributes.load(new File("data/ct_rac_S000_JT00_2011.csv"));
    	System.out.println("Loaded " + attributes.lodesAttributes.keySet().size() + " attributes in " + attributes.attributeGroups.keySet().size() + " attribute groups.");
    	
    	Indicator indicator = new Indicator("workforce", "workforce", blocks, attributes);
    	
    	Long totalJobs = 0l;
    	for(IndicatorItem item : indicator.data) {
    		for(Long value : item.attributes.values()){
    			totalJobs += value;
    		}
    	}
    	
    	System.out.println("total jobs in survey area: " + totalJobs);
    	
    	CSVReader reader = new CSVReader(new FileReader(new File("data/batch_blocks.csv")));
	    
		String [] nextLine;	
		
		HashMap<String, Long> blockJobAccessMap = new HashMap<String, Long>();
		
	    while ((nextLine = reader.readNext()) != null) {
	    	if(nextLine.length > 2)
	    		blockJobAccessMap.put(nextLine[0], Long.parseLong(nextLine[1]));
	    }
	    reader.close();
	    
	    for(String blockId : blocks.lodesBlocks.keySet()) {
	    	if(blockJobAccessMap.containsKey(blockId)) {
	    		blocks.lodesBlocks.get(blockId).attributes.put("jobpct", (long)Math.round(((float)blockJobAccessMap.get(blockId) / totalJobs) * 100));
	    	}
	    }
	    
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.writeValue(new File("output/workforce.json"), indicator);
 
    	
    	App.buildBlocks();
    	//App.buildHaltonPopulation();
    
    }
    
    public static void buildBlocks() throws IOException, FactoryException {
    	ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
    	
    	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Block");
        builder.setCRS(DefaultGeographicCRS.WGS84); 

        // add attributes in order
        builder.add("Block", Polygon.class);
        builder.length(16).add("id", String.class); 
        builder.length(16).add("jobpct", Float.class); 
   
        for(String attribute : attributes.attributeGroups.keySet()) {
        	builder.add(attribute, Long.class); 
        }

        Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", new File("output/blocks.shp").toURL());
		params.put("create spatial index", Boolean.TRUE);
		
		ShapefileDataStore dataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
		dataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        
        // build the type
        final SimpleFeatureType BLOCKS_TYPE = builder.buildFeatureType();
        
        dataStore.createSchema(BLOCKS_TYPE);
        
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(BLOCKS_TYPE);
        
        for(String blockId : blocks.lodesBlocks.keySet()) {
        	if(blocks.lodesBlocks.get(blockId).percentLand < 0.5)
        		continue;
        	
        	featureBuilder.add(blocks.lodesBlocks.get(blockId).block);
        	
        	featureBuilder.add(blockId);
        	featureBuilder.add(blocks.lodesBlocks.get(blockId).attributes.get("jobpct"));
        	
        	for(String attributeId : attributes.attributeGroups.keySet()) {
        		
        		HashMap<String, Long> lodesAttribute = attributes.lodesAttributes.get(blockId);
        		if(lodesAttribute != null) {
        			Long attributeValue = lodesAttribute.get(attributeId);
        			featureBuilder.add(attributeValue);
        		}
        		else {
        			//System.out.println("blockId: " + blockId + " is missing " + attributeId);
        		}
        	}
        	
            SimpleFeature feature = featureBuilder.buildFeature(null);
            featureCollection.add(feature);
            
        }
        
        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) 
        {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
           
            featureStore.addFeatures(featureCollection);
            transaction.commit();

            transaction.close();
        } 
    }
    
    public static void buildHaltonPopulation() throws IOException, FactoryException {
    	ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
    	
    	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Block");
        builder.setCRS(DefaultGeographicCRS.WGS84); 

        // add attributes in order
        builder.add("Jobs", MultiPoint.class);
        builder.length(16).add("id", String.class); 
        builder.length(5).add("type", String.class);
   
        Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", new File("output/halton.shp").toURL());
		params.put("create spatial index", Boolean.TRUE);
		
		ShapefileDataStore dataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
		dataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        
        // build the type
        final SimpleFeatureType BLOCKS_TYPE = builder.buildFeatureType();
        
        dataStore.createSchema(BLOCKS_TYPE);
        
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(BLOCKS_TYPE);
        
        for(String blockId : blocks.lodesBlocks.keySet()) {
        	
        	IndicatorItem block = blocks.lodesBlocks.get(blockId);
        	   	
        	
        	for(String attributeId : attributes.attributeGroups.keySet()) {
        		
        		HashMap<String, Long> lodesAttribute = attributes.lodesAttributes.get(blockId);
        		if(lodesAttribute != null) {
	        		long attributeValue = lodesAttribute.get(attributeId);
	        		
	        		if (attributeValue < Integer.MIN_VALUE || attributeValue > Integer.MAX_VALUE) {
	        	        System.out.println(blockId + " " + attributeId + " exceeds int val max: " + attributeValue);
	        	    } 
	        		else {
	        			featureBuilder.add(block.haltonPoints((int)attributeValue));
	            		featureBuilder.add(blockId);
	            		featureBuilder.add(attributeId);
	            		
	            		SimpleFeature feature = featureBuilder.buildFeature(null);
	                    featureCollection.add(feature);
	        		}
        		}
        		else {
        			//System.out.println("blockId: " + blockId + " is missing " + attributeId);
        		}
        	}   
        }
        
        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) 
        {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
           
            featureStore.addFeatures(featureCollection);
            transaction.commit();

            transaction.close();
        } 
    }
    
}
