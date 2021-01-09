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
	 * A RegEx to match files/folders in the source directory
	 */
	private String fileNamePattern;
	
	/**
	 * Whether the util should consider just files, just directories or both mixed. Defaults to just files.
	 */
	private FileType fileType = FileType.FILES;
	
	/**
	 * The number of most recent files/folders to keep (the util orders by date modified, moves all files/folders except the first {@code numberMostRecentFilesToKeep})
	 */
	private Integer numberMostRecentFilesToKeep;
	
	/**
	 * The action to be performed on files/folders to be archived
	 */
	private ArchiveAction archiveAction;
	
	/**
	 * The destination folder in case {@code archiveAction} = {@code MOVE}
	 */
	private String targetDirectoryName;
	
	/**
	 * When {@code archiveAction} = {@code MOVE}, this specifies if the files/folders are to be moved directly to the target folder (false, default),
	 * or if a new subdirectory with the current time should be created inside it.
	 */
	private Boolean useTargetDirectoryTimestampSubdirectories;
	
	/**
	 * Possible values of the archive action properry
	 */
	public enum ArchiveAction {
		
		MOVE, DELETE;
	}
	
	/**
	 * Possible values of the type of files to be considered
	 */
	public enum FileType {
		
		FILES, DIRECTORIES, BOTH;
	}
}
