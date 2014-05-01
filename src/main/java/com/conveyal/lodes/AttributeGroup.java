package com.conveyal.lodes;

import java.util.ArrayList;

public class AttributeGroup {
	
	public String name;
	public String id;
	public String color;
	
	public ArrayList<String> attributes = new ArrayList<String>();
	
	public AttributeGroup(String [] data) {
		
		name = data[0];
		
		id = data[1];
		color = data[2];
		
		for(int i = 3; i < data.length; i++) {
			if(data[i].isEmpty())
			 	continue;
			 	
			attributes.add(data[i]);
		}
	}	
	
	public AttributeGroup() {
		
	}
	
}
