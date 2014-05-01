package com.conveyal.lodes;

import java.util.Collection;


public class Indicator {

	public String id;
	public String name;
	
	public Collection<AttributeGroup> attributes;
	
	public Collection<IndicatorItem> data;
	
	public Indicator(String id, String name, Blocks blocks, Attributes attributes) {
		
		this.id = id;
		this.name = name;
		this.attributes = attributes.attributeGroups.values();
		
		for(IndicatorItem item : blocks.lodesBlocks.values()) {
			
			if(attributes.lodesAttributes.containsKey(item.geoId)) {
				item.attributes = attributes.lodesAttributes.get(item.geoId);
			}
		}
		
		data = blocks.lodesBlocks.values();		
	}
	
	 public Indicator() {
		 
	 }
}
