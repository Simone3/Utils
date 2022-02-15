package com.utils.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.utils.model.Category;
import com.utils.model.JenkinsData;
import com.utils.model.Parameter;
import com.utils.model.Build;

public class Validator {

	public void validateJenkinsData(JenkinsData jenkinsData) {
		
		if(jenkinsData == null) {
			
			throw new IllegalStateException("No Jenkins data provided");
		}
		
		if(StringUtils.isBlank(jenkinsData.getBaseUrl())) {
			
			throw new IllegalStateException("No Jenkins base URL provided");
		}
		
		if(StringUtils.isBlank(jenkinsData.getCrumbUrl())) {
			
			throw new IllegalStateException("No Jenkins crumb URL provided");
		}
		
		if(StringUtils.isBlank(jenkinsData.getUsername())) {
			
			throw new IllegalStateException("No Jenkins username provided");
		}
		
		if(StringUtils.isBlank(jenkinsData.getPassword())) {
			
			throw new IllegalStateException("No Jenkins password provided");
		}
		
		validateCategories(jenkinsData.getCategories());
	}

	private void validateCategories(List<Category> categories) {
		
		if(categories == null || categories.isEmpty()) {
			
			throw new IllegalStateException("No categories defined");
		}
		
		Map<String, Void> names = new HashMap<>();
		
		for(var i = 0; i < categories.size(); i++) {
			
			var category = categories.get(i);
			
			if(category == null) {
				
				throw new IllegalStateException("Category " + i + " is empty");
			}
			
			String name = category.getName();
			
			if(StringUtils.isBlank(name)) {
				
				throw new IllegalStateException("Category " + i + " has no name");
			}
			
			if(names.containsKey(name)) {
				
				throw new IllegalStateException("Category name " + name + " is repeated");
			}
			
			names.put(name, null);
			
			validateBuilds(name, category.getBuilds());
		}
	}

	private void validateBuilds(String categoryName, List<Build> builds) {
		
		if(builds == null || builds.isEmpty()) {
			
			throw new IllegalStateException("No builds defined for category " + categoryName);
		}
		
		Map<String, Void> names = new HashMap<>();
		
		for(var i = 0; i < builds.size(); i++) {
			
			var build = builds.get(i);
			
			if(build == null) {
				
				throw new IllegalStateException("Build " + i + " of " + categoryName + " is empty");
			}
			
			String name = build.getName();
			
			if(StringUtils.isBlank(name)) {
				
				throw new IllegalStateException("Build " + i + " of " + categoryName + " has no name");
			}
			
			if(StringUtils.isBlank(build.getUrl())) {
				
				throw new IllegalStateException("Build " + name + " of " + categoryName + " has no URL");
			}
			
			if(names.containsKey(name)) {
				
				throw new IllegalStateException("Build name " + name + " is repeated in " + categoryName);
			}
			
			names.put(name, null);
			
			validateParameters(name, build.getParameters());
		}
	}
	
	private void validateParameters(String buildName, List<Parameter> parameters) {
		
		if(parameters == null || parameters.isEmpty()) {
			
			throw new IllegalStateException("No parameters defined for build " + buildName);
		}
		
		Map<String, Void> keys = new HashMap<>();
		
		for(var i = 0; i < parameters.size(); i++) {
			
			var parameter = parameters.get(i);
			
			if(parameter == null) {
				
				throw new IllegalStateException("Parameter " + i + " of " + buildName + " is empty");
			}
			
			String key = parameter.getKey();
			
			if(StringUtils.isBlank(key)) {
				
				throw new IllegalStateException("Parameter " + i + " of " + buildName + " has no key");
			}
			
			boolean hasFixedValue = !StringUtils.isBlank(parameter.getValue());
			boolean hasDynamicValue = parameter.isAskMe();
			
			if((hasFixedValue && hasDynamicValue) || (!hasFixedValue && !hasDynamicValue)) {
				
				throw new IllegalStateException("Parameter " + key + " of " + buildName + " must EITHER have a fixed value or the run-time prompt");
			}
			
			if(keys.containsKey(key)) {
				
				throw new IllegalStateException("Parameter key " + key + " is repeated in " + buildName);
			}
			
			keys.put(key, null);
		}
	}
}
