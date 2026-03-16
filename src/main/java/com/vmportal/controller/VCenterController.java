package com.vmportal.controller;

import com.vmportal.dto.DeployVmRequest;
import com.vmportal.dto.GuacConnectionCreateRequest;
import com.vmportal.dto.GuacConnectionCreateResponse;
import com.vmportal.dto.OpenRemoteConsoleRequest;
import com.vmportal.service.GuacamoleService;
import com.vmportal.service.VCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class VCenterController {

	private final VCenterService vCenterService;
	private final GuacamoleService guacamoleService;

	public VCenterController(VCenterService vCenterService, GuacamoleService guacamoleService) {
		this.vCenterService = vCenterService;
		this.guacamoleService = guacamoleService;
	}

	@GetMapping("/api/vcenter/test")
	public ResponseEntity<Map<String, Object>> testVCenterConnection() {
		Map<String, Object> result = vCenterService.testConnection();
		boolean success = Boolean.TRUE.equals(result.get("success"));
		return success ? ResponseEntity.ok(result) : ResponseEntity.status(500).body(result);
	}

	@GetMapping("/api/vcenter/validate")
	public ResponseEntity<Map<String, Object>> validateDeployResources() {
		Map<String, Object> result = vCenterService.validateDeployResources();
		boolean success = Boolean.TRUE.equals(result.get("success"));
		return success ? ResponseEntity.ok(result) : ResponseEntity.status(500).body(result);
	}

	@PostMapping("/api/vcenter/deploy")
	public ResponseEntity<Map<String, Object>> deployVm(@RequestBody DeployVmRequest request) {
		Map<String, Object> result = vCenterService.deployVm(request);
		boolean success = Boolean.TRUE.equals(result.get("success"));
		return success ? ResponseEntity.ok(result) : ResponseEntity.status(500).body(result);
	}

	@PostMapping("/api/guacamole/open")
	public ResponseEntity<Map<String, Object>> openRemoteConsole(@RequestBody OpenRemoteConsoleRequest request) {
		Map<String, Object> result = new LinkedHashMap<>();

		try {
			if (request.getIpAddress() == null || request.getIpAddress().isBlank()) {
				throw new IllegalArgumentException("ipAddress는 필수입니다.");
			}

			String osType = request.getOsType() != null ? request.getOsType().toLowerCase() : "linux";
			String protocol = osType.contains("windows") ? "rdp" : "ssh";
			String port = osType.contains("windows") ? "3389" : "22";

			String username = (request.getUsername() != null && !request.getUsername().isBlank())
				? request.getUsername()
				: (osType.contains("windows") ? "Administrator" : "fisa");

			String connectionName = (request.getVmName() != null && !request.getVmName().isBlank())
				? request.getVmName()
				: "vm-" + request.getIpAddress();

			GuacConnectionCreateRequest guacReq = GuacConnectionCreateRequest.builder()
				.connectionName(connectionName)
				.protocol(protocol)
				.hostname(request.getIpAddress())
				.port(port)
				.username(username)
				.password(request.getPassword())
				.build();

			GuacConnectionCreateResponse guacRes = guacamoleService.createConnection(guacReq);

			result.put("success", true);
			result.put("message", "원격 화면 URL 생성 성공");
			result.put("vmName", request.getVmName());
			result.put("ipAddress", request.getIpAddress());
			result.put("protocol", protocol);
			result.put("port", port);
			result.put("username", username);
			result.put("guacConnectionId", guacRes.getConnectionId());
			result.put("guacIdentifier", guacRes.getIdentifier());
			result.put("guacUrl", guacRes.getUrl());
			result.put("rawMessage", guacRes.getRawMessage());

			return ResponseEntity.ok(result);

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "원격 화면 URL 생성 실패");
			result.put("error", e.getClass().getSimpleName());
			result.put("detail", e.getMessage());
			return ResponseEntity.status(500).body(result);
		}
	}
}