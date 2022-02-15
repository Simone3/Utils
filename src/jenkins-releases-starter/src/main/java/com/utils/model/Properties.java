package com.utils.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Helper POJO that contains the parsed application properties
 */
@Data
@ConfigurationProperties(ignoreInvalidFields = true, ignoreUnknownFields = true)
public class Properties {

	private boolean disableJenkinsInvocations;
	private boolean printPassword;
	private boolean insecureHttps;
	private int timeoutMilliseconds;
	private JenkinsData jenkins;
}
