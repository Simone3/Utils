package com.utils.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import com.utils.logic.Logic;

/**
 * ApplicationRunner that simply starts the main logic component
 */
public class Runner implements ApplicationRunner {

	private final Logic logic;
	
	public Runner(Logic logic) {
		
		this.logic = logic;
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		logic.startUtil();
	}
}
