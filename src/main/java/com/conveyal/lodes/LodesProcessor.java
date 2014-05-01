package com.conveyal.lodes;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;

public class LodesProcessor {

	Geometry boundary;
	
	Blocks blocks = new Blocks();
	
	Attributes attributes;
	
	public LodesProcessor(File attributeGroupCsv) throws IOException {
		attributes = new Attributes(attributeGroupCsv);
	}
	
	public void setBoundary(Geometry geom) {
		boundary = geom;
	}
	
	public void addShapefile(File blockShape) throws IOException, FactoryException {
		blocks.load(blockShape, boundary);
	}
	
	public void addAddtributes(File csvFile) throws IOException, FactoryException {
		attributes.load(csvFile);
	}
	
	public void createIndicator(String id, String name, File outputFile) throws JsonGenerationException, JsonMappingException, IOException {
		
		System.out.println("Createing indicator " + name +  " (" + id + ") from " + attributes.lodesAttributes.keySet().size() + " attributes in " + attributes.attributeGroups.keySet().size() + " attribute groups.");
		
		Indicator indicator = new Indicator(id, name, blocks, attributes);
		
		GeoJsonModule geojsonModule = new GeoJsonModule();
	
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(geojsonModule);
		mapper.writeValue(outputFile, indicator);
	
	}
	    
		
	public void buildBlocks(File shapeFile) throws IOException, FactoryException {
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	    builder.setName("Block");
	    builder.setCRS(DefaultGeographicCRS.WGS84); 
	
	    // add attributes in order
	    builder.add("Block", Polygon.class);
	    builder.length(16).add("id", String.class); 
	    for(String attribute : attributes.attributeGroups.keySet()) {
	    	builder.add(attribute, Long.class); 
	    }
	
	    Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", shapeFile.toURI().toURL());
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
	    	
	    	featureBuilder.add(blocks.lodesBlocks.get(blockId).geom);
	    	
	    	featureBuilder.add(blockId);
	    	
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

	public void buildHaltonPopulation(File shapeFile) throws IOException, FactoryException {
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	    builder.setName("Block");
	    builder.setCRS(DefaultGeographicCRS.WGS84); 
	
	    // add attributes in order
	    builder.add("Jobs", MultiPoint.class);
	    builder.length(16).add("id", String.class); 
	    builder.length(5).add("type", String.class);
	
	    Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", shapeFile.toURI().toURL());
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
