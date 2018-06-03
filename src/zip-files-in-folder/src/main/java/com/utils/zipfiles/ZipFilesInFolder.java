package com.utils.zipfiles;

import com.utils.common.core.StringUtils;
import com.utils.common.files.FileInfos;
import com.utils.common.files.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Given a folder and a number of files-per-zip, creates zip file(s) containing all files in the folder
 */
public class ZipFilesInFolder {

	private static final int    BUFFER_SIZE   = 1024;
	private static final String PART_PREFIX   = "_part";
	private static final String ZIP_EXTENSION = ".zip";

	public List<ZipResult> process(String filesFolderName, int filesPerZip, String zipFilesPrefix, boolean confirm) throws IllegalStateException {

		// Basic validation
		if(filesPerZip <= 0) {
			
			throw new IllegalStateException("filesPerZip <= 0");
		}
		if(filesPerZip > 1 && StringUtils.isEmpty(zipFilesPrefix)) {

			throw new IllegalStateException("If files per zip > 1, must have a zip filename prefix");
		}
		
		// Source folder validation
		final File filesFolder = FileUtils.getValidFolder(filesFolderName);
		final File[] files = filesFolder.listFiles();
		if(files == null || files.length == 0) {

			throw new IllegalStateException("No files in given directory");
		}

		// Set util vars
		List<ZipResult> preview = new ArrayList<>();
		boolean prefixIsFilename = false;
		if(filesPerZip == 1 && StringUtils.isEmpty(zipFilesPrefix)) {
			
			prefixIsFilename = true;
		}
		
		// Sort files by name
		Arrays.sort(files);
		
		// Create byte buffer for file reading
		byte[] buffer = new byte[BUFFER_SIZE];
		
		// Create zip(s) for each file
		try {
			
			ZipOutputStream zipOutputStream = null;
			ZipResult previewZip = null;
			for(int i = 0; i < files.length; i++) {
				
				FileInfos fileInfos = new FileInfos(files[i]);

				if(i % filesPerZip == 0) {
					
					// Close current zip file
					if(confirm && i != 0) {
						
						zipOutputStream.close();
					}
					
					// Open new zip file
					String zipFileName = (prefixIsFilename ? fileInfos.getFileName() : zipFilesPrefix + PART_PREFIX + i) + ZIP_EXTENSION;
					String zipFullFileName = fileInfos.getPath() + File.separator + zipFileName;
					if(confirm) {
						
						FileOutputStream fileOutputStream = new FileOutputStream(zipFullFileName);
						zipOutputStream = new ZipOutputStream(fileOutputStream);
					}
					else {

						previewZip = new ZipResult(zipFileName);
						preview.add(previewZip);
					}
				}
				
				if(confirm) {
					
					// Begin writing current file in the zip file
					FileInputStream fileInputStream = new FileInputStream(fileInfos.getFullFileName());
					zipOutputStream.putNextEntry(new ZipEntry(fileInfos.getFileName()));
					
					// Read and write current file
					int length;
					while((length = fileInputStream.read(buffer)) > 0) {
						
						zipOutputStream.write(buffer, 0, length);
					}

					// Current file is completed
					zipOutputStream.closeEntry();
					fileInputStream.close();
				}
				else {

					previewZip.getContainedFiles().add(fileInfos.getFileName());
				}
			}
			
			// Close last zip file
			if(confirm) {
				
				zipOutputStream.close();
			}

			return preview;
		}
		catch(IOException ioe) {
			
			throw new IllegalStateException("Error creating zip file: " + ioe);
		}
	}
}