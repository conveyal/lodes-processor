package com.conveyal.lodes_processor;

import java.io.File;
import java.io.IOException;

import org.opengis.referencing.FactoryException;

import com.conveyal.lodes.Attributes;
import com.conveyal.lodes.Blocks;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     * @throws IOException 
     * @throws FactoryException 
     */
    public void testShapeLoader() throws IOException, FactoryException
    {
    	Blocks blocks = new Blocks();
    	blocks.load(new File("data/nyc_example.shp"), null);
    	
        assertTrue( true );
    }
    
    public void testCsvLoader() throws IOException, FactoryException
    {
    	Attributes attributes = new Attributes(new File("data/attribute_groups.csv"));
    	attributes.load(new File("data/ny_rac_S000_JT01_2011.csv"));
    	System.out.println("Loaded " + attributes.lodesAttributes.keySet().size() + " attributes in " + attributes.attributeGroups.keySet().size() + " attribute groups.");
        assertTrue( true );
    }
    
    
}
