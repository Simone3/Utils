package com.utils.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.utils.logic.Properties.ArchiveAction;
import com.utils.logic.Properties.FileType;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for the util logic.
 * This util moves or deletes all files/folders from a folder except the N most recent.
 */
@Slf4j
@AllArgsConstructor
public class Logic {

	private static final DateTimeFormatter SUBDIRECTORY_NAME_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss.S");
	
	private final Properties properties;
	
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
			
			log.error("ERROR: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
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
		
		log.info("Start checking \"{}\"...", directory.getAbsolutePath());
		
		return directory;
	}
	
	/**
	 * Gets the target directory, with validation
	 * @return the valid target directory
	 */
	private File getTargetDirectory() {
		
		var directoryName = properties.getTargetDirectoryName();
		Assert.isTrue(!StringUtils.isBlank(directoryName), "A target directory name must be specified via properties");
		var directory = getDirectory(directoryName);
		
		var useTargetDirectoryTimestampSubdirectories = properties.getUseTargetDirectoryTimestampSubdirectories();
		if(useTargetDirectoryTimestampSubdirectories != null && useTargetDirectoryTimestampSubdirectories) {
			
			ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
			var subdirectoryName = now.format(SUBDIRECTORY_NAME_DATE_PATTERN);
			return createDirectory(directory.getAbsolutePath() + File.separator + subdirectoryName);
		}
		else {
		
			return directory;
		}
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
	 * Helper to create a directory
	 * @param directoryName the directory name
	 * @return the valid directory
	 */
	private File createDirectory(String directoryName) {
		
		var directory = new File(directoryName);
		
		Assert.isTrue(!directory.exists(), directoryName + " already exists");
		
		if(properties.isSafeMode()) {

			log.info("[Safe Mode] This would create subdirectory \"{}\"", directory);
		}
		else {

			var created = directory.mkdir();
			
			Assert.isTrue(created, directoryName + " was not created");
		}
		
		return directory;
	}
	
	/**
	 * Gets all matching files in the directory, with validation
	 * @param directory the source directory
	 * @return the list of matching files
	 */
	private List<File> getFiles(File directory) {

		var fileType = properties.getFileType();
		var fileNamePatternString = properties.getFileNamePattern();
		
		// Get all files and folders in the source directory
		var filesStream = Stream.of(directory.listFiles());
		
		// Filter by file type if necessary
		if(fileType != FileType.BOTH) {
			
			log.info("Considering only {}...", fileType);
			filesStream = filesStream.filter(file -> isFileTypeValid(file, fileType));
		}
		
		// Filter by file name if necessary
		if(!StringUtils.isBlank(fileNamePatternString)) {

			var fileNamePattern = Pattern.compile(fileNamePatternString);
			log.info("Considering only files that match {}...", fileNamePattern);
			filesStream = filesStream.filter(file -> isFileNameValid(file, fileNamePattern));
		}
		
		// Sort and collect all matching files
		var files = filesStream
			.sorted(Comparator.comparingLong(File::lastModified).reversed())
			.collect(Collectors.toList());

		log.info("Found {} files that match the constraints", files.size());
		
		return files;
	}
	
	/**
	 * Helper to filter files by desired type
	 * @param file the current file
	 * @param fileType the type of files to be considered
	 * @return true if the file is a match
	 */
	private boolean isFileTypeValid(File file, FileType fileType) {
		
		if(fileType == FileType.FILES) {
			
			return file.isFile();
		}
		else if(fileType == FileType.DIRECTORIES) {
			
			return file.isDirectory();
		}
		else {
			
			throw new IllegalStateException("Unhandled file type: " + fileType);
		}
	}

	/**
	 * Helper to filter files by desired file name
	 * @param file the current file
	 * @param fileNamePattern the RegEx
	 * @return true if the file is a match
	 */
	private boolean isFileNameValid(File file, Pattern fileNamePattern) {
		
		return fileNamePattern.matcher(file.getName()).find();
	}
	
	/**
	 * Helper to start the archiving process on the list of files
	 * @param files the files
	 */
	private void processFiles(List<File> files) throws IOException {
		
		// No files warning
		if(files.isEmpty()) {
			
			log.warn("No matching files in folder");
			return;
		}
		
		// Get the number of files to keep from properties
		var numberMostRecentFilesToKeep = properties.getNumberMostRecentFilesToKeep();
		Assert.isTrue(numberMostRecentFilesToKeep != null && numberMostRecentFilesToKeep >= 0, "The number of most recent files to keep must be specified");
		
		// No need to do anything if already lower than expected files
		if(files.size() <= numberMostRecentFilesToKeep) {
			
			log.info("No need to archive: {} files in directory ({} most recent files to be kept)", files.size(), numberMostRecentFilesToKeep);
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
			
			log.info("[Safe Mode] This would move \"{}\" to \"{}\"", file.getAbsolutePath(), targetDirectory.getAbsolutePath());
		}
		else {
			
			Files.move(Paths.get(file.getAbsolutePath()), Paths.get(targetDirectory.getAbsolutePath() + File.separator + file.getName()));
			log.info("Moved \"{}\" to \"{}\"", file.getAbsolutePath(), targetDirectory.getAbsolutePath());
		}
	}

	/**
	 * Helper to delete a file
	 * @param file the file
	 */
	private void deleteFile(File file) throws IOException {
		
		if(properties.isSafeMode()) {
			
			log.info("[Safe Mode] This would delete \"{}\"", file.getAbsolutePath());
		}
		else {

			Files.delete(Paths.get(file.getAbsolutePath()));
			log.info("Deleted \"{}\"", file.getAbsolutePath());
		}
	}
}
