package com.vmportal.service;

import static com.vmware.vim25.ws.TrustAllSSL.*;

import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmportal.dto.CreateProvisionRequestDto;
import com.vmportal.entity.ProvisionRequest;
import com.vmportal.entity.ProvisionStatus;
import com.vmportal.repository.ProvisionRequestRepository;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ServiceInstance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

@Service
@RequiredArgsConstructor
public class ProvisionRequestService {

	private final ProvisionRequestRepository provisionRequestRepository;

	public void createRequest(CreateProvisionRequestDto dto) {
		String templateName = resolveTemplateName(dto.getOsType());

		ProvisionRequest request = ProvisionRequest.builder()
			.vmName(dto.getVmName())
			.osType(dto.getOsType())
			.cpu(dto.getCpu())
			.memoryGb(dto.getMemoryGb())
			.templateName(templateName)
			.clusterName("Seoul-Cluster")
			.resourcePoolName("Student-Lab")
			.networkName("PG-172.30.10")
			.datastoreName("datastore1")
			.status(ProvisionStatus.REQUESTED)
			.createdAt(LocalDateTime.now())
			.build();

		provisionRequestRepository.save(request);
	}

	public List<ProvisionRequest> getAllRequests() {
		return provisionRequestRepository.findAll();
	}

	private String resolveTemplateName(String osType) {
		if ("Ubuntu".equalsIgnoreCase(osType)) {
			return "rocky-custom";
		} else if ("Windows".equalsIgnoreCase(osType)) {
			return "tmpl-win-2022";
		}
		return "unknown-template";
	}












}