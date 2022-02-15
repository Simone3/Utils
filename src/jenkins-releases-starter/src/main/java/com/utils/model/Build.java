package com.utils.model;

import java.util.List;

import lombok.Data;

@Data
public class Build implements SelectOption {
	
	private String name;
	private String url;
	private List<Parameter> parameters;
	
	@Override
	public String getOptionName() {
		
		return name;
	}
}
