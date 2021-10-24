package com.utils.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.utils.config.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UtilLogicRunner implements CommandLineRunner {

	@Autowired
	private Properties properties;

	@Override
	public void run(String... args) throws Exception {
		
		// TODO logic
		log.info("TODO logic");
	}
}
