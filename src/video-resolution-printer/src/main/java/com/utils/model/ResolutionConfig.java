package com.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResolutionConfig {

	private int heightFrom;
	private int heightTo;
	private String label;
}
