package com.vmportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "guacamole")
public class GuacamoleProperties {
	private String baseUrl;
	private String username;
	private String password;
	private String datasource;
}