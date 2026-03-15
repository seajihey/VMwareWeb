package com.vmportal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuacConnectionCreateRequest {
	private String connectionName;
	private String protocol;   // ssh
	private String hostname;   // 10.30.10.55
	private String port;       // 22
	private String username;   // rocky
	private String password;   // 비밀번호
}