package com.conveyal.lodes;

import java.util.ArrayList;

public class AttributeGroup {
	
	public String name;
	public String id;
	
	public ArrayList<String> attributes = new ArrayList<String>();
	
	public AttributeGroup(String [] data) {
		
		name = data[0];
		
		id = data[1];
		
		for(int i = 2; i < data.length; i++) {
			attributes.add(data[i]);
		}
	}	
	
	public AttributeGroup() {
		
	}
	
}
