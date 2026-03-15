package com.vmportal.service;

import com.vmportal.config.GuacamoleProperties;
import com.vmportal.dto.GuacConnectionCreateRequest;
import com.vmportal.dto.GuacConnectionCreateResponse;
import com.vmportal.dto.GuacLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuacamoleService {

	private final GuacamoleProperties properties;
	private final RestTemplate restTemplate = new RestTemplate();

	public GuacLoginResponse login() {
		String url = properties.getBaseUrl() + "/api/tokens";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("username", properties.getUsername());
		form.add("password", properties.getPassword());

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);
		ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

		Map body = response.getBody();
		if (body == null) {
			throw new IllegalStateException("Guacamole 로그인 응답이 비어 있습니다.");
		}

		Object authToken = body.get("authToken");
		Object dataSource = body.get("dataSource");

		if (authToken == null) {
			throw new IllegalStateException("Guacamole authToken을 받지 못했습니다. 응답: " + body);
		}
		if (dataSource == null) {
			throw new IllegalStateException("Guacamole dataSource를 받지 못했습니다. 응답: " + body);
		}

		return new GuacLoginResponse(authToken.toString(), dataSource.toString());
	}
	private String buildGuacClientIdentifier(String connectionId, String dataSource) {
		String raw = connectionId + '\0' + "c" + '\0' + dataSource;
		return Base64.getEncoder()
			.withoutPadding()
			.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
	}
	public GuacConnectionCreateResponse createConnection(GuacConnectionCreateRequest req) {
		GuacLoginResponse login = login();

		String url = properties.getBaseUrl()
			+ "/api/session/data/"
			+ login.getDataSource()
			+ "/connections?token="
			+ login.getAuthToken();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("hostname", req.getHostname());
		parameters.put("port", req.getPort() != null ? req.getPort() : "22");
		parameters.put("username", req.getUsername() != null ? req.getUsername() : "");
		parameters.put("password", req.getPassword() != null ? req.getPassword() : "");
		parameters.put("security", "any");

		Map<String, String> attributes = new LinkedHashMap<>();
		attributes.put("max-connections", "");
		attributes.put("max-connections-per-user", "");
		attributes.put("weight", "");
		attributes.put("failover-only", "");
		attributes.put("guacd-encryption", "");
		attributes.put("guacd-hostname", "");
		attributes.put("guacd-port", "");
		attributes.put("guacd-ssl", "");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("parentIdentifier", "ROOT");
		payload.put("name", req.getConnectionName() + "-" + System.currentTimeMillis());
		payload.put("protocol", req.getProtocol());
		payload.put("parameters", parameters);
		payload.put("attributes", attributes);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
		ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

		Map body = response.getBody();
		if (body == null) {
			throw new IllegalStateException("Guacamole connection 생성 응답이 비어 있습니다.");
		}

		String connectionId = body.get("identifier") != null ? body.get("identifier").toString()
			: body.get("id") != null ? body.get("id").toString()
			: null;

		String encodedIdentifier = buildGuacClientIdentifier(connectionId, login.getDataSource());
		String guacUrl = properties.getBaseUrl() + "/#/client/" + encodedIdentifier;
		return GuacConnectionCreateResponse.builder()
			.success(true)
			.connectionId(connectionId)
			.identifier(encodedIdentifier)
			.url(guacUrl)
			.rawMessage(body.toString())
			.build();	}
}