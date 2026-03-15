package com.vmportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "provision_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisionRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vm_name", nullable = false)
	private String vmName;

	@Column(name = "os_type", nullable = false)
	private String osType;

	@Column(nullable = false)
	private Integer cpu;

	@Column(name = "memory_gb", nullable = false)
	private Integer memoryGb;

	@Column(name = "template_name")
	private String templateName;

	@Column(name = "cluster_name")
	private String clusterName;

	@Column(name = "resource_pool_name")
	private String resourcePoolName;

	@Column(name = "network_name")
	private String networkName;

	@Column(name = "datastore_name")
	private String datastoreName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProvisionStatus status;

	@Column(name = "vc_task_id")
	private String vcTaskId;

	@Column(name = "vm_id")
	private String vmId;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "remote_protocol")
	private String remoteProtocol;

	@Column(name = "remote_port")
	private Integer remotePort;

	@Column(name = "remote_username")
	private String remoteUsername;

	@Column(name = "guac_ready")
	private Boolean guacReady;

	@Column(name = "guac_connection_id")
	private String guacConnectionId;

	@Column(name = "guac_identifier")
	private String guacIdentifier;

	@Column(name = "guac_url", length = 1000)
	private String guacUrl;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}