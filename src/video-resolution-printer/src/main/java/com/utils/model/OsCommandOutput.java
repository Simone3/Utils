package com.utils.model;

import java.util.List;

import lombok.Data;

@Data
public class OsCommandOutput {

	private int exitValue;
	private List<String> outputLines;
}
