package com.utils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.utils.model.Properties;

@SpringBootApplication
@EnableConfigurationProperties(Properties.class)
public class UtilApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(UtilApplication.class, args);
	}
}
