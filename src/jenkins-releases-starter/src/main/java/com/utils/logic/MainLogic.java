package com.utils.logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.utils.model.Build;
import com.utils.model.Category;
import com.utils.model.JenkinsData;
import com.utils.model.Properties;
import com.utils.model.Step;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainLogic {

	private CommandLineInterface cli;
	private final Validator validator;
	private final JenkinsCaller caller;

	private final boolean printPassword;
	private final JenkinsData jenkinsData;
	
	private Step step = Step.PICK_CATEGORY;
	private Category category;
	private int currentBuildIndex = 0;
	private List<Build> builds;
	private Map<String, String> parameterValues;
	
	public MainLogic(Properties properties) {
		
		this.cli = new CommandLineInterface();
		this.validator = new Validator();
		this.caller = new JenkinsCaller(properties.isDisableJenkinsInvocations(), properties.isInsecureHttps(), properties.getTimeoutMilliseconds());
		this.jenkinsData = properties.getJenkins();
		this.printPassword = properties.isPrintPassword();
	}
	
	public void execute() {
		
		try {
		
			cli.println();
			cli.println("********* START *********");
			
			processJenkinsData();
			loopSteps();
			
			cli.println();
			cli.println("********* END *********");
			cli.println();
		}
		catch(Exception e) {
			
			cli.printError("GENERIC EXCEPTION: " + e.getMessage());
			log.error("Generic error in main logic", e);
		}
	}
	
	private void processJenkinsData() {
		
		try {
			
			validator.validateJenkinsData(jenkinsData);
		}
		catch(Exception e) {
			
			cli.printError("Invalid configuration: " + e.getMessage());
			log.error("Validation error", e);
			printSampleConfiguration();
			step = Step.EXIT;
		}
	}
	
	private void loopSteps() {
		
		while(step != Step.EXIT) {
			
			switch(step) {
			
				case PICK_CATEGORY:
					pickCategory();
					break;
					
				case PICK_BUILDS:
					pickBuilds();
					break;
					
				case DEFINE_PARAMETERS:
					defineParameters();
					break;
				
				case START_BUILD:
					startBuild();
					break;
					
				case ASK_ANOTHER:
					askAnother();
					break;
					
				default:
					throw new IllegalStateException("Unknown step: " + step);
			}
		}
	}
	
	private void pickCategory() {
		
		var allCategories = jenkinsData.getCategories();
		category = cli.askUserSelection("Categories", allCategories);
		step = Step.PICK_BUILDS;
	}
	
	private void pickBuilds() {
		
		var allBuilds = category.getBuilds();
		builds = cli.askUserSelectionMultiple("Builds", allBuilds);
		currentBuildIndex = 0;
		step = Step.DEFINE_PARAMETERS;
	}
	
	private void defineParameters() {
		
		var build = builds.get(currentBuildIndex);
		
		parameterValues = new LinkedHashMap<>();
		
		for(var parameter: build.getParameters()) {
			
			String key = parameter.getKey();
			
			String value;
			if(parameter.isAskMe()) {
				
				String whitespaceNote = parameter.isRemoveWhitespace() ? " (all spaces will be removed)" : "";
				value = cli.getUserInput("Value for \"" + key + "\" parameter for build \"" + build.getName() + "\"" + whitespaceNote + ": ");
			}
			else {
				
				value = parameter.getValue();
			}
			
			if(parameter.isRemoveWhitespace()) {
				
				value = value.replaceAll("\\s+", "");
				value = value.replace("\u200B", "");
			}

			parameterValues.put(key, value);
		}
		
		step = Step.START_BUILD;
	}
	
	private void startBuild() {
		
		var build = builds.get(currentBuildIndex);

		String categoryName = category.getName();
		String buildName = build.getName();
		String crumbUrl = jenkinsData.getBaseUrl() + jenkinsData.getCrumbUrl();
		String buildUrl = jenkinsData.getBaseUrl() + build.getUrl();
		String username = jenkinsData.getUsername();
		String password = jenkinsData.getPassword();
		Map<String, String> parameters = parameterValues;
		
		cli.println();
		cli.println("Start build #" + (currentBuildIndex + 1) + " with:");
		cli.println("  - Category: " + categoryName);
		cli.println("  - Name: " + buildName);
		cli.println("  - Crumb URL: " + crumbUrl);
		cli.println("  - Build URL: " + buildUrl);
		cli.println("  - Username: " + username);
		cli.println("  - Password: " + (printPassword ? password : "******"));
		cli.println("  - Parameters:");
		for(var paramEntry: parameters.entrySet()) {
			
			cli.println("     - " + paramEntry.getKey() + ": " + paramEntry.getValue());
		}
		
		if(cli.askUserConfirmation("Start build")) {
			
			startBuildWithRetries(buildName, crumbUrl, buildUrl, username, password, parameters);
		}
		
		if(currentBuildIndex >= builds.size() - 1) {
			
			step = Step.ASK_ANOTHER;
		}
		else {
			
			currentBuildIndex++;
			step = Step.DEFINE_PARAMETERS;
		}
	}

	private boolean startBuildWithRetries(String buildName, String crumbUrl, String buildUrl, String username, String password, Map<String, String> parameters) {
		
		boolean first = true;
		
		while(first || cli.askUserConfirmation("Retry build \"" + buildName + "\"")) {
			
			first = false;
			
			try {
				
				cli.println();
				cli.println("Starting build \"" + buildName + "\"...");
				
				caller.startBuild(crumbUrl, buildUrl, username, password, parameters);
				
				cli.println("Build \"" + buildName + "\" started successfully!");
				
				return true;
			}
			catch(Exception e) {
				
				cli.printError("Cannot start build \"" + buildName + "\": " + e.getMessage());
				log.error("Error starting build", e);
			}
		}
		
		return false;
	}
	
	private void askAnother() {
		
		if(cli.askUserConfirmation("Pick another category/build")) {
			
			clearState();
			step = Step.PICK_CATEGORY;
		}
		else {
			
			step = Step.EXIT;
		}
	}
	
	private void clearState() {
		
		this.category = null;
		this.currentBuildIndex = 0;
		this.builds = null;
		this.parameterValues = null;
	}
	
	@SneakyThrows
	private void printSampleConfiguration() {
		
		cli.println();
		cli.println("Define an application.yml in the JAR folder similar to this one:");
		cli.println();
		cli.println("--------------");

		var sampleConfiguration = new ClassPathResource("application-sample.yml", this.getClass().getClassLoader());
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(sampleConfiguration.getInputStream()))) {
			
			String line = reader.readLine();
			while(line != null) {
					
				cli.println(line);
				line = reader.readLine();
			}
		}
		
		cli.println();
		cli.println("--------------");
	}
}
