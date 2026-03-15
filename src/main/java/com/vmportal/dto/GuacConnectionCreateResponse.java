package com.vmportal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuacConnectionCreateResponse {
	private boolean success;
	private String connectionId;
	private String identifier;
	private String url;
	private String rawMessage;
}