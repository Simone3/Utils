package com.utils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.utils.logic.Properties;

@SpringBootApplication
@EnableConfigurationProperties(Properties.class)
public class Application {

	public static void main(String[] args) {
		
		SpringApplication.run(Application.class, args);
	}
}
