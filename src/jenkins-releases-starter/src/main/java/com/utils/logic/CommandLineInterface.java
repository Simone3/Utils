package com.utils.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.utils.model.SelectOption;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandLineInterface {
	
	private static final String INPUT_YES = "y";
	private static final String INPUT_NO  = "n";

	private final Scanner scanner;
	
	public CommandLineInterface() {
		
		this.scanner = new Scanner(System.in);
	}
	
	public String getUserInput(String message) {
		
		String input = null;
		
		while(StringUtils.isBlank(input)) {
			
			println();
			print(message);
			input = scanner.nextLine();
		}
		
		return input.trim();
	}
	
	public boolean askUserConfirmation(String message) {
		
		String confirm = null;
		
		while(!INPUT_YES.equalsIgnoreCase(confirm) && !INPUT_NO.equalsIgnoreCase(confirm)) {
			
			confirm = getUserInput(message + " (" + INPUT_YES + "/" + INPUT_NO + ")? ");
		}
		
		return INPUT_YES.equalsIgnoreCase(confirm);
	}
	
	public <T extends SelectOption> T askUserSelection(String message, List<T> options) {
		
		return askUserSelection(message, options, false).get(0);
	}
	
	public <T extends SelectOption> List<T> askUserSelectionMultiple(String message, List<T> options) {
		
		return askUserSelection(message, options, true);
	}
	
	private <T extends SelectOption> List<T> askUserSelection(String message, List<T> options, boolean multiple) {
		
		int size = options.size();
		
		if(size == 0) {
			
			throw new IllegalStateException("Unexpected empty options list!");
		}
		else if(size == 1) {
			
			T onlyOption = options.get(0);
			
			println();
			println(message + ": picked only option \"" + onlyOption.getOptionName() + "\"");
			
			return List.of(onlyOption);
		}
		else {
			
			println();
			println(message + ":");
			for(var i = 0; i < size; i++) {
				
				println("  " + (i + 1) + ". " + options.get(i).getOptionName());
			}
			
			List<T> pickedOptions = null;
			
			while(pickedOptions == null) {
			
				var selectionString = multiple ? getUserInput("Pick one or more options (1-" + size + ", comma separated): ") : getUserInput("Pick one option (1-" + size + "): ");
				
				try {
				
					var indices = parseOptionIndices(selectionString, size, multiple);
					
					pickedOptions = new ArrayList<>();
					
					for(var index: indices) {
						
						pickedOptions.add(options.get(index));
					}
				}
				catch(Exception e) {
					
					log.error("Error parsing option indices", e);
					pickedOptions = null;
				}
			}
			
			return pickedOptions;
		}
	}
	
	private List<Integer> parseOptionIndices(String selectionString, int size, boolean multiple) {
		
		List<Integer> indices = new ArrayList<>();
		
		var optionStrings = selectionString.split("\\s*,\\s*");
		
		for(var optionString: optionStrings) {
			
			var optionNumber = Integer.valueOf(optionString);
			
			if(optionNumber <= 0 || optionNumber > size) {
				
				throw new IllegalStateException("Option " + optionNumber + " is out of range");
			}
			
			indices.add(optionNumber - 1);
		}
		
		indices = indices.stream().sorted().distinct().collect(Collectors.toList());
		
		if(!multiple && indices.size() > 1) {
			
			throw new IllegalStateException("Cannot select more than one option in a non-multiple select");
		}
		
		return indices;
	}

	public void print(String message) {
		
		System.out.print(message);
	}

	public void println() {
		
		System.out.println();
	}
	
	public void println(String message) {
		
		System.out.println(message);
	}
	
	public void printError(String error) {
		
		println();
		System.out.println("[ERROR] " + error);
	}
}
