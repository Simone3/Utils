package com.utils.model;

import java.util.List;

import lombok.Data;

@Data
public class Category implements SelectOption {

	private String name;
	private List<Build> builds;
	
	@Override
	public String getOptionName() {
		
		return name;
	}
}
