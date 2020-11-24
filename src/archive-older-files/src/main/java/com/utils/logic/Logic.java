package com.utils.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.utils.logic.Properties.ArchiveAction;

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
		
		try {
		
			var directory = getSourceDirectory();
			var files = getFiles(directory);
			processFiles(files);
		}
		catch(Exception e) {
			
			LOGGER.error("ERROR: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the source directory, with validation
	 * @return the valid source directory
	 */
	private File getSourceDirectory() {
		
		var directoryName = properties.getSourceDirectoryName();
		Assert.isTrue(!StringUtils.isBlank(directoryName), "A source directory name must be specified via properties");
		
		var directory = getDirectory(directoryName);
		
		LOGGER.info("Start checking \"{}\"...", directory.getAbsolutePath());
		
		return directory;
	}
	
	/**
	 * Gets the target directory, with validation
	 * @return the valid target directory
	 */
	private File getTargetDirectory() {
		
		var directoryName = properties.getTargetDirectoryName();
		
		Assert.isTrue(!StringUtils.isBlank(directoryName), "A target directory name must be specified via properties");
		
		return getDirectory(directoryName);
	}
	
	/**
	 * Helper to get and validate a directory
	 * @param directoryName the directory name
	 * @return the valid directory
	 */
	private File getDirectory(String directoryName) {
		
		var directory = new File(directoryName);
		
		Assert.isTrue(directory.exists(), directoryName + " does not exist");
		Assert.isTrue(directory.isDirectory(), directoryName + " is not a directory");
		
		return directory;
	}
	
	/**
	 * Gets all matching files in the directory, with validation
	 * @param directory the source directory
	 * @return the list of matching files
	 */
	private List<File> getFiles(File directory) {
		
		var fileNamePatternString = properties.getFileNamePattern();
		var fileNamePattern = Pattern.compile(fileNamePatternString);
		
		var files = Stream.of(directory.listFiles())
			.filter(file -> isFileValid(file, fileNamePattern))
			.sorted(Comparator.comparingLong(File::lastModified).reversed())
			.collect(Collectors.toList());

		LOGGER.info("Found {} files that match {}", files.size(), fileNamePattern);
		
		return files;
	}
	
	/**
	 * Helper to filter files
	 * @param file the current file
	 * @param fileNamePattern the RegEx
	 * @return true if the file is a match
	 */
	private boolean isFileValid(File file, Pattern fileNamePattern) {
		
		return file.isFile() && fileNamePattern.matcher(file.getName()).find();
	}
	
	/**
	 * Helper to start the archiving process on the list of files
	 * @param files the files
	 */
	private void processFiles(List<File> files) throws IOException {
		
		// No files wrning
		if(files.isEmpty()) {
			
			LOGGER.warn("No matching files in folder");
			return;
		}
		
		// Get the number of files to keep from properties
		var numberMostRecentFilesToKeep = properties.getNumberMostRecentFilesToKeep();
		Assert.isTrue(numberMostRecentFilesToKeep != null && numberMostRecentFilesToKeep >= 0, "The number of most recent files to keep must be specified");
		
		// No need to do anything if already lower than expected files
		if(files.size() <= numberMostRecentFilesToKeep) {
			
			LOGGER.info("No need to archive: {} files in directory ({} most recent files to be kept)", files.size(), numberMostRecentFilesToKeep);
			return;
		}
		
		// Get the active action
		var archiveAction = properties.getArchiveAction();
		Assert.isTrue(archiveAction != null, "An archive action among " + Arrays.toString(ArchiveAction.values()) + " must be specified");
		
		// Get if necessary the target directory
		File targetDirectory = null;
		if(archiveAction == ArchiveAction.MOVE) {
			
			targetDirectory = getTargetDirectory();
		}

		// Archive all files skipping the "numberMostRecentFilesToKeep" most recent ones
		for(var i = numberMostRecentFilesToKeep; i < files.size(); i++) {
			
			var file = files.get(i);
			
			if(archiveAction == ArchiveAction.MOVE) {
				
				moveFile(file, targetDirectory);
			}
			else if(archiveAction == ArchiveAction.DELETE) {
				
				deleteFile(file);
			}
			else {
				
				throw new IllegalStateException("Archive action " + archiveAction + " not handled");
			}
		}
	}

	/**
	 * Helper to move a file
	 * @param file the file
	 * @param targetDirectory the target directory
	 */
	private void moveFile(File file, File targetDirectory) throws IOException {
		
		if(properties.isSafeMode()) {
			
			LOGGER.info("[Safe Mode] I would move \"{}\" to \"{}\"", file.getAbsolutePath(), targetDirectory.getAbsolutePath());
		}
		else {
			
			Files.move(Paths.get(file.getAbsolutePath()), Paths.get(targetDirectory.getAbsolutePath() + File.separator + file.getName()));
			LOGGER.info("Moved \"{}\" to \"{}\"", file.getAbsolutePath(), targetDirectory.getAbsolutePath());
		}
	}

	/**
	 * Helper to delete a file
	 * @param file the file
	 */
	private void deleteFile(File file) throws IOException {
		
		if(properties.isSafeMode()) {
			
			LOGGER.info("[Safe Mode] I would delete \"{}\"", file.getAbsolutePath());
		}
		else {

			Files.delete(Paths.get(file.getAbsolutePath()));
			LOGGER.info("Deleted \"{}\"", file.getAbsolutePath());
		}
	}
}
