package com.utils.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.utils.application.Runner;
import com.utils.logic.Logic;
import com.utils.logic.Properties;

/**
 * List of Spring components for this util
 */
@Configuration
public class BeanConfiguration {
		
	@Bean
	public Logic logic(Properties properties) {
		
		return new Logic(properties);
	}

	@Bean
	public Runner runner(Logic logic) {
		
		return new Runner(logic);
	}
}
