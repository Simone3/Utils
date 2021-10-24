package com.utils.logic;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.utils.config.Properties;
import com.utils.model.OutputEmailAddress;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UtilLogicRunner implements CommandLineRunner {
	
	@Autowired
	private Properties properties;

	@Override
	public void run(String... args) throws Exception {
		
		log.info("Start email backup parser util");
		
		Assert.isTrue(args != null && args.length == 2, "Util requires 2 arguments, source and target files");
		
		var sourceFile = getSourceFile(args);
		var targetFile = getTargetFile(args);
		
		var allEmailAddresses = parseSourceFile(sourceFile);
		
		var sortedEmailAddresses = sortEmailAddresses(allEmailAddresses);
		
		writeOutput(targetFile, sortedEmailAddresses);
	}

	private File getSourceFile(String... args) {
		
		var sourceFilePath = args[0];
		var sourceFile = new File(sourceFilePath);
		
		Assert.isTrue(sourceFile.exists(), "Source file does not exist");
		Assert.isTrue(sourceFile.isFile(), "Source file is not a file");
		
		return sourceFile;
	}

	private File getTargetFile(String... args) {
		
		var targetFilePath = args[1];
		var targetFile = new File(targetFilePath);
		
		if(targetFile.exists()) {
			
			Assert.isTrue(targetFile.isFile(), "Target file is not a file");
			
			log.warn("Replacing target file");
		}
		
		return targetFile;
	}

	private Collection<OutputEmailAddress> parseSourceFile(File sourceFile) {
		
		var sourceFileExtension = FilenameUtils.getExtension(sourceFile.getAbsolutePath()).toLowerCase();
		
		switch(sourceFileExtension) {
		
			case "olm":
				return parseOlm(sourceFile);
				
			case "pst":
			case "ost":
				throw new IllegalStateException("TODO implement parsing of PST and OST files too!");
				
			default:
				throw new IllegalStateException("Unrecognized source file extension: " + sourceFileExtension);
		}
	}
	
	private Collection<OutputEmailAddress> parseOlm(File sourceFile) {
		
		log.info("Start parsing OML {}", sourceFile.getAbsolutePath());
		
		var olmParser = new OlmParser(sourceFile, properties.getLogFrequency());
		Collection<OutputEmailAddress> allEmailAddresses = olmParser.getAllEmailAddresses();
		
		log.info("Parsed OML {}", sourceFile.getAbsolutePath());
		
		return allEmailAddresses;
	}
	
	private List<OutputEmailAddress> sortEmailAddresses(Collection<OutputEmailAddress> allEmailAddresses) {
		
		return allEmailAddresses
			.stream()
			.sorted(Comparator
				.comparing(OutputEmailAddress::getFirst)
				.thenComparing(OutputEmailAddress::getLast)
				.thenComparing(OutputEmailAddress::getDomain)
				.thenComparing(OutputEmailAddress::getAddress)
			)
			.collect(Collectors.toList());
	}
	
	@SneakyThrows
	private void writeOutput(File targetFile, List<OutputEmailAddress> sortedEmailAddresses) {
		
		log.info("Start writing output {}", targetFile.getAbsolutePath());
		
		try(var writer = new FileWriter(targetFile.getAbsolutePath())) {

			for(var address: sortedEmailAddresses) {
				
				writer.write(
					address.getDomain() + "\t" +
					address.getAddress() + "\t" +
					address.getName() + "\t" +
					address.getOccurrences() + "\t" +
					address.getFirst() + "\t" +
					address.getLast() + System.lineSeparator());
			}
		}
		
		log.info("Written output {}", targetFile.getAbsolutePath());
	}
}
