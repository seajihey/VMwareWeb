package com.vmportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenRemoteConsoleRequest {
	private String vmName;
	private String osType;      // linux / windows
	private String ipAddress;
	private String username;
	private String password;
}