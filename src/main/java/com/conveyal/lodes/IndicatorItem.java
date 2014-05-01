package com.conveyal.lodes;

import java.util.HashMap;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class IndicatorItem {
	
	static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	public String geoId;
	
	public double lat;
	public double lon;
	
	public HashMap<String, Long> attributes  = new HashMap<String, Long>();
	
	public Geometry geom;
	public Point centroid;
	
	Double percentLand;
	
	public IndicatorItem(String geoId, Geometry block, Double percentLand) {
		
		this.geoId = geoId;
		this.geom = block;
		this.centroid = block.getCentroid();
		
		this.lat = this.centroid.getY();
		this.lon = this.centroid.getX();
		
		this.percentLand = percentLand;
	}
	
	public IndicatorItem() {
		 
	}

	public Geometry haltonPoints(int numberPoints) {
		
		int basei = 2;
		int basej = 3;
		
		Envelope env = this.geom.getEnvelopeInternal();
		Coordinate[] pts = new Coordinate[numberPoints];
		
		double baseX = env.getMinX();
		double baseY = env.getMinY();
	    
		int i = 0;
		int j = 0;
		
		while (i < numberPoints) {
			
			double x = baseX + env.getWidth() * haltonNumber(j + 1, basei);
			double y = baseY + env.getHeight() * haltonNumber(j + 1, basej);
			Coordinate p = new Coordinate(x, y);
			
			j++;
			if (!this.geom.contains(geometryFactory.createPoint(p)))
				continue;
			pts[i++] = p;
		}
		
		return geometryFactory.createMultiPoint(pts);
		
	}
	  
	private double haltonNumber(int index, int base) {
		double result = 0;
		double fraction = 1.0 / base;
		int i = index;
		while (i > 0) {
			result = result + fraction * (i % base);
			i = (int) Math.floor(i / (double) base);
			fraction = fraction / base;
		}
		return result;
	}
}
