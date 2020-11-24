package com.utils.logic;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Helper POJO that contains the parsed application properties
 */
@Data
@ConfigurationProperties
public class Properties {

	/**
	 * If the safe mode is on (actions are just logged, no real archiving is done)
	 */
	private boolean safeMode = false;
	
	/**
	 * The directory in which the util should work
	 */
	private String sourceDirectoryName;
	
	/**
	 * A RegEx to match files in the source directory
	 */
	private String fileNamePattern;
	
	/**
	 * The number of most recent files to keep (the util orders by date modified, moves all files except the first {@code numberMostRecentFilesToKeep})
	 */
	private Integer numberMostRecentFilesToKeep;
	
	/**
	 * The action to be performed on files to be archived
	 */
	private ArchiveAction archiveAction;
	
	/**
	 * The destination folder in case {@code archiveAction} = {@code MOVE}
	 */
	private String targetDirectoryName;
	
	/**
	 * Possible values of the archive action properry
	 */
	public enum ArchiveAction {
		
		MOVE, DELETE;
	}
}
