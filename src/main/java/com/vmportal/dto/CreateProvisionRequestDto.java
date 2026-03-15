package com.vmportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProvisionRequestDto {
	private String vmName;
	private String osType;
	private Integer cpu;
	private Integer memoryGb;
	private String ipAddress;
	private String subnetMask;
	private String gateway;
	private String dns;
}