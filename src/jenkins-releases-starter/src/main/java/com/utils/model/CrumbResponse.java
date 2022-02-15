package com.utils.model;

import lombok.Data;

@Data
public class CrumbResponse {

	private String _class;
	private String crumb;
	private String crumbRequestField;
}
