package com.conveyal.lodes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedPolygon;

public class Blocks {

	public HashMap<String, IndicatorItem> lodesBlocks = new HashMap<String, IndicatorItem>();

	CoordinateReferenceSystem wgsCRS = DefaultGeographicCRS.WGS84;
	
	public Blocks() {
	
	}
	
	public void load(File blockShapefile, Geometry boundary) throws IOException, FactoryException {
		
		System.out.println("loading " + blockShapefile.getName());
		
		PreparedPolygon preparedBoundary =  null;
		
		if(boundary !=null)
			preparedBoundary = new PreparedPolygon((Polygonal)boundary);
		
		Map map = new HashMap();
		map.put( "url", blockShapefile.toURI().toURL() );
		
		DataStore dataStore = DataStoreFinder.getDataStore(map);
		
		SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]); 
		
		SimpleFeatureType schema = featureSource.getSchema();

		CoordinateReferenceSystem shpCRS = schema.getCoordinateReferenceSystem();
		MathTransform transform = CRS.findMathTransform(shpCRS, wgsCRS, true);

		SimpleFeatureCollection collection = featureSource.getFeatures();
		SimpleFeatureIterator iterator = collection.features();
		
		Integer skippedFeatures = 0;
		Integer clipedFeatures = 0;
		
		try {
			while( iterator.hasNext() ) {
		         
				try {
					
					SimpleFeature feature = iterator.next();
			    	String geoId = (String)feature.getAttribute("GEOID10");
			         
			    	Long areaLand = (Long) feature.getAttribute("ALAND10");
			    	Long areaWater = (Long) feature.getAttribute("AWATER10");
			         
			        Double percentLand = (double) (areaLand / (areaLand + areaWater));
			         
			        Geometry geom = JTS.transform((Geometry)feature.getDefaultGeometry(),  transform);
			        Point centroid = geom.getCentroid();
			        if(preparedBoundary == null || (preparedBoundary.contains(centroid)))
			        	lodesBlocks.put(geoId, new IndicatorItem(geoId, geom, percentLand));
			        else
			        	clipedFeatures++;
			   
				}
				catch(Exception e) {
					skippedFeatures++;
					System.out.println(e.toString());
					continue;
				}
		     }
		}
		finally {
		     iterator.close();
		} 
		
		dataStore.dispose();
		
		System.out.println("Features imported: " + lodesBlocks.size() + "(" + skippedFeatures + " skipped, " + clipedFeatures + " outside survey area)");
	}
}
