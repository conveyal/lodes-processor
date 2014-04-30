package com.conveyal.lodes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.opengis.referencing.FactoryException;

import au.com.bytecode.opencsv.CSVReader;

public class Attributes {

/*	
    1 h_geocode Char15 Residence Census Block Code
	2 C000 Num Total number of jobs 
	3 CA01 Num Number of jobs for workers age 29 or younger 
	4 CA02 Num Number of jobs for workers age 30 to 54 
	5 CA03 Num Number of jobs for workers age 55 or older 
	6 CE01 Num Number of jobs with earnings $1250/month or less 
	7 CE02 Num Number of jobs with earnings $1251/month to $3333/month 
	8 CE03 Num Number of jobs with earnings greater than $3333/month 
	9 CNS01 Num Number of jobs in NAICS sector 11 (Agriculture, Forestry, Fishing and Hunting) 
	10 CNS02 Num Number of jobs in NAICS sector 21 (Mining, Quarrying, and Oil and Gas Extraction)
	11 CNS03 Num Number of jobs in NAICS sector 22 (Utilities) 
	12 CNS04 Num Number of jobs in NAICS sector 23 (Construction) 
	13 CNS05 Num Number of jobs in NAICS sector 31-33 (Manufacturing) 
	14 CNS06 Num Number of jobs in NAICS sector 42 (Wholesale Trade) 
	15 CNS07 Num Number of jobs in NAICS sector 44-45 (Retail Trade) 
	16 CNS08 Num Number of jobs in NAICS sector 48-49 (Transportation and Warehousing) 
	17 CNS09 Num Number of jobs in NAICS sector 51 (Information) 
	18 CNS10 Num Number of jobs in NAICS sector 52 (Finance and Insurance) 
	19 CNS11 Num Number of jobs in NAICS sector 53 (Real Estate and Rental and Leasing) 
	20 CNS12 Num Number of jobs in NAICS sector 54 (Professional, Scientific, and Technical Services)
	21 CNS13 Num Number of jobs in NAICS sector 55 (Management of Companies and Enterprises) 
	22 CNS14 Num Number of jobs in NAICS sector 56 (Administrative and Support and Waste Management and Remediation Services) 
	23 CNS15 Num Number of jobs in NAICS sector 61 (Educational Services) 
	24 CNS16 Num Number of jobs in NAICS sector 62 (Health Care and Social Assistance) 
	25 CNS17 Num Number of jobs in NAICS sector 71 (Arts, Entertainment, and Recreation) 
	26 CNS18 Num Number of jobs in NAICS sector 72 (Accommodation and Food Services) 
	27 CNS19 Num Number of jobs in NAICS sector 81 (Other Services [except Public Administration]) 
	28 CNS20 Num Number of jobs in NAICS sector 92 (Public Administration)
	
	Construction & Manufacturing (CM): CNS01,CNS02,CNS04,CNS05
	Sector 11 (Agriculture, forestry, fishing and hunting)
	Sector 21 (Mining)
	Sector 23 (Construction)
	Sector 31-33 (Manufacturing)
	
	Trade (T): CNS06,CNS07
	Sector 42 (Wholesale trade)
	Sector 44-45 (Retail trade)

	Transportation & Utilities (TU): CNS08,CNS03
	Sector 48-49 (Transportation and warehousing)
	Sector 22 (Utilities)

	Information (I): CNS09
	Sector 51 (Information)

	Financial activities (F): CNS10,CNS11
	Sector 52 (Finance and insurance)
	Sector 53 (Real estate and rental and leasing)

	Professional & business services (PB): CNS12,CNS13,CNS14
	Sector 54 (Professional, scientific, and technical services)
	Sector 55 (Management of companies and enterprises)
	Sector 56 (Administrative and support and waste management and remediation services)

	Education services (E): CNS15
	Sector 61 (Education services)

	Health services (H): CNS16
	Sector 62 (Health care and social assistance)

	Leisure, hospitality and other services (LH): CNS17,CNS18,CNS19
	Sector 71 (Arts, entertainment, and recreation)
	Sector 72 (Accommodation and food services)
	Sector 81 (Other services, except public administration)

	Public administration (P): CNS20
	Sector 92 (Public administration) 

*/
	public HashMap<String, HashMap<String, Long>> lodesAttributes = new HashMap<String, HashMap<String, Long>>();
	
	public HashMap<String, AttributeGroup> attributeGroups = new HashMap<String, AttributeGroup>();
	
	public Attributes(File groupsFile) throws IOException {
		// TODO load this from a config file
		
		System.out.println("loading " + groupsFile.getName());
		
		CSVReader reader = new CSVReader(new FileReader(groupsFile));
	    
		String [] nextLine;	
		
	    while ((nextLine = reader.readNext()) != null) {
	    	
	    	AttributeGroup group = new AttributeGroup(nextLine);
	    	attributeGroups.put(group.id, group);
	    }	
	    
	    reader.close();
	}
	
	public void load(File attributeFile) throws IOException, FactoryException {
		
		System.out.println("loading " + attributeFile.getName());
		
		CSVReader reader = new CSVReader(new FileReader(attributeFile));
	    
		String [] headerLine = reader.readNext();
		
		HashMap<String,Integer> headerMap = new HashMap<String,Integer>();
		
		Integer headerPos = 0;
		
		for(String header : headerLine) {
			
			headerMap.put(header, headerPos);		
			headerPos++;
		}
		
		String [] lodesLine;	
		
	    while ((lodesLine = reader.readNext()) != null) {
	    	
	    	String geoId = lodesLine[0];
	    	
	    	lodesAttributes.put(geoId, new HashMap<String, Long>());
	    	
	    	for(String attributeId : attributeGroups.keySet()) {
	    	
	    		AttributeGroup group = attributeGroups.get(attributeId);
	    		
	    		Long attributeTotal = 0l;
	    		
	    		for(String column : group.attributes) {
	    			if(!headerMap.containsKey(column))
	    				System.err.println("column " + column + " not found.");
	    			
	    			String value = lodesLine[headerMap.get(column)];
	    			if(value != null) {
	    				
	    				if(value.contains("E3"))
	    					value = value.replace("E3", "000");
	    				
	    				attributeTotal += Long.parseLong(value);
	    				
	    			}
	    				
	    				
	    				
	    		}
	    		
	    		lodesAttributes.get(geoId).put(attributeId, attributeTotal);
	    	}
	    }
	    
	    reader.close();
	}
}
