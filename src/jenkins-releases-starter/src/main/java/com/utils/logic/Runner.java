package com.utils.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.utils.model.Properties;

@Component
public class Runner implements CommandLineRunner {

	@Autowired
	private Properties properties;

	@Override
	public void run(String... args) throws Exception {
		
		MainLogic logic = new MainLogic(properties);
		logic.execute();
	}
}
