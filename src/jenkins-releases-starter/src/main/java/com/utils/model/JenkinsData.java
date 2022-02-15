package com.utils.model;

import java.util.List;

import lombok.Data;

@Data
public class JenkinsData {

	private String baseUrl;
	private String crumbUrl;
	private String username;
	private String password;
	private List<Category> categories;
}
