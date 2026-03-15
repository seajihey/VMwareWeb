package com.vmportal.dto;

public class GuacLoginResponse {

	private String authToken;
	private String dataSource;

	public GuacLoginResponse() {
	}

	public GuacLoginResponse(String authToken, String dataSource) {
		this.authToken = authToken;
		this.dataSource = dataSource;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
}