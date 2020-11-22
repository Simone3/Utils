package com.utils.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the util logic
 */
public class Logic {

	private static final Logger LOGGER = LoggerFactory.getLogger(Logic.class);
	
	private final Properties properties;
	
	public Logic(Properties properties) {
		
		this.properties = properties;
	}
	
	/**
	 * Starts the util logic
	 */
	public void startUtil() {
		
		LOGGER.info("Util logic here: {}", properties);
	}
}
