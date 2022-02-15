package com.utils.model;

import lombok.Data;

@Data
public class Parameter {

	private String key;
	private String value;
	private boolean askMe;
	private boolean removeWhitespace;
}
