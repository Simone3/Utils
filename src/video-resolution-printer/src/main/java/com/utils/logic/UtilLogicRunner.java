package com.utils.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utils.common.files.FileUtils;
import com.utils.model.FfprobeOutput;
import com.utils.model.FfprobeStream;
import com.utils.model.OsCommandOutput;
import com.utils.model.ResolutionConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UtilLogicRunner implements CommandLineRunner {

	private static final List<String> VIDEO_EXTENSIONS = List.of(
		"3g2",
		"3gp",
		"MTS",
		"M2TS",
		"TS",
		"amv",
		"asf",
		"avi",
		"drc",
		"f4a",
		"f4b",
		"f4p",
		"f4v",
		"flv",
		"gif",
		"gifv",
		"m4v",
		"mkv",
		"mng",
		"mov",
		"qt",
		"mp4",
		"m4p",
		"m4v",
		"mpg",
		"mp2",
		"mpeg",
		"mpe",
		"mpv",
		"mpg",
		"mpeg",
		"m2v",
		"mxf",
		"nsv",
		"ogv",
		"ogg",
		"rm",
		"rmvb",
		"roq",
		"svi",
		"viv",
		"vob",
		"webm",
		"wmv",
		"yuv"
	);
	
	private static final List<String> SUBS_EXTENSIONS = List.of(
		"STL",
		"SRT",
		"890",
		"CIP",
		"PAC",
		"SCC",
		"SUB"
	);
	
	private static final List<ResolutionConfig> RESOLUTION_CONFIGS = List.of(
		new ResolutionConfig( 200,  250,  "240p"),
		new ResolutionConfig( 250,  370,  "360p"),
		new ResolutionConfig( 370,  490,  "480p"),
		new ResolutionConfig( 490,  730,  "720p"),
		new ResolutionConfig( 730, 1090, "1080p"),
		new ResolutionConfig(1090, 1450, "1440p"),
		new ResolutionConfig(1450, 2170, "2160p"),
		new ResolutionConfig(2170, 4330, "4320p")
	);
	
	private ObjectMapper mapper;
	
	public UtilLogicRunner() {
		
		mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		if(args == null || args.length == 0 || StringUtils.isBlank(args[0])) {
			
			throw new IllegalStateException("You must pass the folder as the first argument!");
		}
		
		File folder = FileUtils.getValidFolder(args[0]);
		
		File[] files = getSortedFiles(folder);
		
		printHeader();
		handleFiles(folder, files);
	}

	private File[] getSortedFiles(File folder) {
		
		File[] files = folder.listFiles();
		if(files == null || files.length == 0) {
		
			throw new IllegalStateException(folder.getAbsolutePath() + " contains no files");
		}
		
		Arrays.sort(files);
		
		return files;
	}

	private void handleFiles(File folder, File[] files) {
		
		for(int i = 0; i < files.length; i++) {
			
			File file = files[i];
			
			try {
				
				String baseName = FilenameUtils.getBaseName(file.getName());
				String extension = FilenameUtils.getExtension(file.getName());
	
				if(isExtension(extension, VIDEO_EXTENSIONS)) {
					
					handleVideoFile(folder, file, baseName, extension, "VIDEO");
				}
				else if(isExtension(extension, SUBS_EXTENSIONS)) {
					
					handleSubFile(folder, file, baseName, extension, files, i);
				}
				else {
					
					printFile(file, "UNKNOWN", null, null, null, null);
				}
			}
			catch(InterruptedException e) {
				
				log.error("Interrupted", e);
				Thread.currentThread().interrupt();
			}
			catch(Exception e) {
				
				log.error("Error on file", e);
				printFile(file, "ERROR", null, null, null, e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
	}
	
	private void handleVideoFile(File folder, File file, String baseName, String extension, String type) throws IOException, InterruptedException {
		
		FfprobeStream stream = runFfprobeCommand(folder, file);
		String resolution = stream.getWidth() + " x " + stream.getHeight();
		String resolutionLabel = guessResolutionLabel(stream);
		String possibleNewFileName = guessNewFileName(baseName, extension, resolutionLabel);
		printFile(file, type, resolution, resolutionLabel, possibleNewFileName, null);
	}
	
	private void handleSubFile(File folder, File file, String baseName, String extension, File[] files, int i) throws IOException, InterruptedException {
		
		File linkedVideoFile = findVideoLinkedToSubs(baseName, files, i);
		if(linkedVideoFile == null) {

			printFile(file, "SUB WITHOUT MATCH", null, null, null, null);
		}
		else {
			
			handleVideoFile(folder, linkedVideoFile, baseName, extension, "SUB WITH MATCH");
		}
	}
	
	private void printHeader() {
	
		print(String.join("\t",
			"NAME",
			"TYPE",
			"RESOLUTION",
			"RESOLUTION LABEL",
			"POSSIBLE NEW FILENAME",
			"NOTES"
		));
	}
	
	private void printFile(File file, String type, String resolution, String resolutionLabel, String possibleNewFileName, String notes) {
		
		print(String.join("\t",
			file.getAbsolutePath(),
			StringUtils.defaultIfEmpty(type, ""),
			StringUtils.defaultIfEmpty(resolution, ""),
			StringUtils.defaultIfEmpty(resolutionLabel, ""),
			StringUtils.defaultIfEmpty(possibleNewFileName, file.getName()),
			StringUtils.defaultIfEmpty(notes, "")
		));
	}
	
	private void print(String text) {
		
		System.out.println(text);
	}
	
	private String guessNewFileName(String baseName, String extension, String resolutionLabel) {
		
		if(resolutionLabel == null) {
			
			return null;
		}
		
		if(baseName.contains("[" + resolutionLabel + "]")) {
			
			return null;
		}
		
		for(ResolutionConfig config: RESOLUTION_CONFIGS) {
			
			if(baseName.contains("[" + config.getLabel() + "]")) {
				
				return baseName.replace("[" + config.getLabel() + "]", "[" + resolutionLabel + "]") + "." + extension;
			}
		}
		
		return baseName + " [" + resolutionLabel + "]" + "." + extension;
	}

	private File findVideoLinkedToSubs(String subBaseName, File[] files, int i) {
		
		if(i > 0) {
			
			File prevFile = files[i - 1];
			if(isSubVideoMatch(subBaseName, prevFile)) {
				
				return prevFile;
			}
		}
		
		if(i < files.length - 1) {
			
			File nextFile = files[i + 1];
			if(isSubVideoMatch(subBaseName, nextFile)) {
				
				return nextFile;
			}
		}
		
		return null;
	}
	
	private boolean isSubVideoMatch(String subBaseName, File otherFile) {
		
		String otherFileBaseName = FilenameUtils.getBaseName(otherFile.getName());
		String otherFileExtension = FilenameUtils.getExtension(otherFile.getName());
		return subBaseName.equals(otherFileBaseName) && isExtension(otherFileExtension, VIDEO_EXTENSIONS);
	}

	private boolean isExtension(String fileExtension, List<String> matchExtensions) {
		
		for(String videoExtension: matchExtensions) {
			
			if(videoExtension.equalsIgnoreCase(fileExtension)) {
				
				return true;
			}
		}
	
		return false;
	}
	
	private String guessResolutionLabel(FfprobeStream stream) {
		
		int smallDimension = stream.getHeight() < stream.getWidth() ? stream.getHeight() : stream.getWidth();
		
		for(ResolutionConfig config: RESOLUTION_CONFIGS) {
			
			if(smallDimension < config.getHeightFrom()) {
				
				return null;
			}
			else if(smallDimension <= config.getHeightTo()) {
				
				return config.getLabel();
			}
		}
		
		return null;
	}
	
	private FfprobeStream runFfprobeCommand(File folder, File file) throws IOException, InterruptedException {
		
		String command = "ffprobe -v error -select_streams v -show_entries stream=width,height -of json \"" + file.getAbsolutePath() + "\"";
		
		OsCommandOutput output = runOsCommand(folder, command);
		
		if(output.getExitValue() != 0) {
			
			throw new IllegalStateException("ffprobe command failed with exit value " + output.getExitValue() + ": " + String.join(" ", output.getOutputLines()));
		}
		
		String outputJson = String.join(" ", output.getOutputLines());
		
		FfprobeOutput ffprobeOutput = mapper.readValue(outputJson, FfprobeOutput.class);
		
		if(ffprobeOutput == null || CollectionUtils.isEmpty(ffprobeOutput.getStreams()) || ffprobeOutput.getStreams().get(0).getWidth() <= 0 || ffprobeOutput.getStreams().get(0).getHeight() <= 0) {
			
			throw new IllegalStateException("Failed to get video resolution");
		}
		
		return ffprobeOutput.getStreams().get(0);
	}
	
	private OsCommandOutput runOsCommand(File folder, String command) throws IOException, InterruptedException {
		
		ProcessBuilder builder;
		if(SystemUtils.IS_OS_WINDOWS) {

			builder = new ProcessBuilder("cmd", "/c", command);
		}
		else {
			
			builder = new ProcessBuilder("sh", "-c", command);
		}
		
		builder.directory(folder);

		Process process = builder.start();

		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

		List<String> outputLines = new ArrayList<>();
		
		String line;
		while((line = input.readLine()) != null) {

			outputLines.add(line);
		}
		
		int exitValue = process.waitFor();

		OsCommandOutput commandOutput = new OsCommandOutput();
		commandOutput.setExitValue(exitValue);
		commandOutput.setOutputLines(outputLines);
		return commandOutput;
	}
}
