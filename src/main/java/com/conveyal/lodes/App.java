package com.conveyal.lodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
    public static void main( String[] args ) throws Exception
    {
    	// create the command line parser
    	CommandLineParser parser = new BasicParser();
    		
    	// create the Options
    	Options options = new Options();
   
    	options.addOption( "s", "shapes", true, "File or directory containing Census block shapes" );
    	options.addOption( "a", "attributes", true, "File or directory containing LODES CSVs" );
    	options.addOption( "ag", "attribute-groups", true, "CSV file defining attribute groups from csv/shapefile columns" );
    	options.addOption( "b", "boundary", true, "GeoJSON file defining boundary (optional)" );
    	
    	options.addOption( "i", "id", true, "indicator id" );
    	options.addOption( "n", "name", true, "indicator name" );
    	
    	options.addOption( "bs", "block-shape", false, "export block shapefiles" );
    	options.addOption( "hs", "halton-shape", false, "export halton dots shapefiles" );
    	
    	try {
    	    // parse the command line arguments
    	    CommandLine line = parser.parse( options, args );
    	    
        	String shapePath = line.getOptionValue("shapes");
        	String attributePath = line.getOptionValue("attributes");
        	String attributeGroupPath = line.getOptionValue("attribute-groups");
        	String boundaryPath = line.getOptionValue("boundary");
        	
        	String id = line.getOptionValue("id");
        	String name = line.getOptionValue("name");
        	
        	
        	Boolean blockShape = line.hasOption("block-shape");
        	Boolean haltonShape = line.hasOption("halton-shape");
        	
        
        	
        	if(attributeGroupPath == null)
        		throw new Exception("Attribute group file not found");
        	
        	File attributeGroup = new File(attributeGroupPath);
        	
        	if(!attributeGroup.exists())
        		throw new Exception("Attribute group file not found");
        	
        	
        	// create processor and load attribute group csv
        	LodesProcessor lodesProcessor = new LodesProcessor(attributeGroup);
        	
        	// define boundary -- an optional input (currently defined as a geojson input but could be any geom format)
        	
        	if(boundaryPath != null) {
        		File boundaryFile = new File(boundaryPath);
            	if(boundaryFile.exists()) {
            		BufferedReader fs = new BufferedReader(new FileReader("data/boundary.json"));
            		GeometryJSON g = new GeometryJSON();
                	Geometry boundary = g.read(fs);
                	
                	lodesProcessor.setBoundary(boundary);
            	}
        	}
        	        	
        	// load one or more census block shapefils
        	File shapeFiles = new File(shapePath);
        	
        	if(!shapeFiles.exists())
        		throw new Exception("Shape data not found");
        		
        	if(shapeFiles.isDirectory()) {
        		
        		for(File shapeFile : shapeFiles.listFiles()) {
        			if(shapeFile.getName().toLowerCase().endsWith(".shp"))
        				lodesProcessor.addShapefile(shapeFile);
        		}
        	}
        	else
        		lodesProcessor.addShapefile(shapeFiles);
        	
        	
        	// load one or more LODES census CSV tables
        	File attributeFiles = new File(attributePath);
        	
        	if(!attributeFiles.exists())
        		throw new Exception("Attribute data not found");
        		
        	if(attributeFiles.isDirectory()) {
        		
        		for(File attributeFile : attributeFiles.listFiles()) {
        			if(attributeFile.getName().toLowerCase().endsWith(".csv"))
        				lodesProcessor.addAddtributes(attributeFile);
        		}
        	}
        	else
        		lodesProcessor.addShapefile(attributeFiles);
        	
        	String outputFileName = id;
        
        	// export indicator json file 
        	lodesProcessor.createIndicator(id, name, new File(outputFileName + ".json"));
        	
        	if(blockShape)	
        		lodesProcessor.buildBlocks(new File("blocks_" + outputFileName + ".shp"));
   
        	if(haltonShape)
        		lodesProcessor.buildHaltonPopulation(new File("halton_" + outputFileName + ".shp"));
        	
    	}
    	catch( ParseException exp ) {
    	    System.out.println( "Unexpected exception:" + exp.getMessage() );
    	}
    	
    }
    
    
}
