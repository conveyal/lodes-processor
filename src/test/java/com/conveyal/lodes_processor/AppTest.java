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
    
        assertTrue( true );
    }
    
    public void testCsvLoader() throws IOException, FactoryException
    {
    	assertTrue( true );
    }
    
    
}
