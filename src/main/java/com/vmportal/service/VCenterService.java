package com.vmportal.service;
import com.vmportal.dto.GuacConnectionCreateRequest;
import com.vmportal.dto.GuacConnectionCreateResponse;
import com.vmware.vim25.CustomizationAdapterMapping;
import com.vmware.vim25.CustomizationFixedIp;
import com.vmware.vim25.CustomizationFixedName;
import com.vmware.vim25.CustomizationGlobalIPSettings;
import com.vmware.vim25.CustomizationIPSettings;
import com.vmware.vim25.CustomizationLinuxPrep;
import com.vmware.vim25.CustomizationSpec;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.vmportal.dto.DeployVmRequest;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmportal.config.VCenterProperties;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Network;

import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.net.URL;
import java.security.cert.X509Certificate;

@Service
public class VCenterService {
	private final GuacamoleService guacamoleService;

	private final VCenterProperties properties;

	public VCenterService(VCenterProperties properties, GuacamoleService guacamoleService) {
		this.properties = properties;
		this.guacamoleService = guacamoleService;
	}
	public Map<String, Object> testConnection() {
		Map<String, Object> result = new LinkedHashMap<>();
		ServiceInstance serviceInstance = null;

		try {
			trustAllHttpsCertificates();
			HostnameVerifier hv = (hostname, session) -> true;
			HttpsURLConnection.setDefaultHostnameVerifier(hv);

			serviceInstance = new ServiceInstance(
				new URL(properties.getUrl()),
				properties.getUsername(),
				properties.getPassword(),
				true
			);

			Folder rootFolder = serviceInstance.getRootFolder();

			Datacenter datacenter = (Datacenter) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Datacenter", properties.getDatacenter());

			ClusterComputeResource cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
				.searchManagedEntity("ClusterComputeResource", properties.getCluster());

			result.put("success", true);
			result.put("message", "vCenter 연결 성공");
			result.put("vcenterUrl", properties.getUrl());
			result.put("datacenterFound", datacenter != null);
			result.put("clusterFound", cluster != null);
			result.put("datacenterName", datacenter != null ? datacenter.getName() : null);
			result.put("clusterName", cluster != null ? cluster.getName() : null);

			if (cluster != null) {
				HostSystem[] hosts = cluster.getHosts();
				result.put("hostCount", hosts != null ? hosts.length : 0);
			} else {
				result.put("hostCount", 0);
			}

			return result;

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "vCenter 연결 실패");
			result.put("error", e.getClass().getSimpleName());
			result.put("detail", e.getMessage());
			return result;
		} finally {
			if (serviceInstance != null) {
				try {
					serviceInstance.getServerConnection().logout();
				} catch (Exception ignored) {
				}
			}
		}
	}

	private void trustAllHttpsCertificates() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			}
		};

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	public Map<String, Object> validateDeployResources() {
		Map<String, Object> result = new LinkedHashMap<>();
		ServiceInstance serviceInstance = null;

		try {
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

			serviceInstance = new ServiceInstance(
				new URL(properties.getUrl()),
				properties.getUsername(),
				properties.getPassword(),
				true
			);

			Folder rootFolder = serviceInstance.getRootFolder();

			Datacenter datacenter = (Datacenter) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Datacenter", properties.getDatacenter());

			ClusterComputeResource cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
				.searchManagedEntity("ClusterComputeResource", properties.getCluster());

			Network network = (Network) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Network", properties.getNetwork());

			Datastore datastore = (Datastore) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Datastore", properties.getDatastore());

			VirtualMachine template = (VirtualMachine) new InventoryNavigator(rootFolder)
				.searchManagedEntity("VirtualMachine", properties.getRockyTemplate());

			result.put("success", true);
			result.put("datacenterFound", datacenter != null);
			result.put("clusterFound", cluster != null);
			result.put("networkFound", network != null);
			result.put("datastoreFound", datastore != null);
			result.put("templateFound", template != null);

			result.put("datacenterName", datacenter != null ? datacenter.getName() : null);
			result.put("clusterName", cluster != null ? cluster.getName() : null);
			result.put("networkName", network != null ? network.getName() : null);
			result.put("datastoreName", datastore != null ? datastore.getName() : null);
			result.put("templateName", template != null ? template.getName() : null);

			if (cluster != null) {
				HostSystem[] hosts = cluster.getHosts();
				List<String> hostNames = new ArrayList<>();

				if (hosts != null) {
					for (HostSystem host : hosts) {
						hostNames.add(host.getName());
					}
				}

				result.put("hostCount", hosts != null ? hosts.length : 0);
				result.put("hostNames", hostNames);
				result.put("resourcePoolName", cluster.getResourcePool() != null ? cluster.getResourcePool().getName() : null);
			}

			if (datastore != null && datastore.getSummary() != null) {
				result.put("datastoreCapacity", datastore.getSummary().getCapacity());
				result.put("datastoreFreeSpace", datastore.getSummary().getFreeSpace());
				result.put("datastoreAccessible", datastore.getSummary().isAccessible());
				result.put("datastoreType", datastore.getSummary().getType());
			}

			if (template != null && template.getConfig() != null) {
				result.put("templateGuestFullName", template.getConfig().getGuestFullName());
				result.put("templateNumCpu", template.getConfig().getHardware().getNumCPU());
				result.put("templateMemoryMb", template.getConfig().getHardware().getMemoryMB());
				result.put("templateIsTemplate", template.getConfig().isTemplate());
			}

			return result;

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "배포 리소스 검증 실패");
			result.put("error", e.getClass().getSimpleName());
			result.put("detail", e.getMessage());
			return result;
		} finally {
			if (serviceInstance != null) {
				try {
					serviceInstance.getServerConnection().logout();
				} catch (Exception ignored) {
				}
			}
		}
	}
	public Map<String, Object> deployVm(DeployVmRequest request) {
		Map<String, Object> result = new LinkedHashMap<>();
		ServiceInstance serviceInstance = null;

		try {
			if (request.getVmName() == null || request.getVmName().isBlank()) {
				throw new IllegalArgumentException("vmName은 필수입니다.");
			}

			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

			serviceInstance = new ServiceInstance(
				new URL(properties.getUrl()),
				properties.getUsername(),
				properties.getPassword(),
				true
			);

			Folder rootFolder = serviceInstance.getRootFolder();

			Datacenter datacenter = (Datacenter) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Datacenter", properties.getDatacenter());
			if (datacenter == null) {
				throw new IllegalStateException("Datacenter를 찾을 수 없습니다.");
			}

			ClusterComputeResource cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
				.searchManagedEntity("ClusterComputeResource", properties.getCluster());
			if (cluster == null) {
				throw new IllegalStateException("Cluster를 찾을 수 없습니다.");
			}

			Datastore datastore = (Datastore) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Datastore", properties.getDatastore());
			if (datastore == null) {
				throw new IllegalStateException("Datastore를 찾을 수 없습니다.");
			}

			String networkName = (request.getNetworkName() != null && !request.getNetworkName().isBlank())
				? request.getNetworkName()
				: properties.getNetwork();

			Network network = (Network) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Network", networkName);
			if (network == null) {
				throw new IllegalStateException("Network를 찾을 수 없습니다: " + networkName);
			}

			String templateName = (request.getTemplateName() != null && !request.getTemplateName().isBlank())
				? request.getTemplateName()
				: properties.getRockyTemplate();

			VirtualMachine template = (VirtualMachine) new InventoryNavigator(rootFolder)
				.searchManagedEntity("VirtualMachine", templateName);
			if (template == null) {
				throw new IllegalStateException("Template을 찾을 수 없습니다: " + templateName);
			}

			Folder vmFolder = datacenter.getVmFolder();
			ResourcePool resourcePool = cluster.getResourcePool();

			VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();
			relocateSpec.setDatastore(datastore.getMOR());
			relocateSpec.setPool(resourcePool.getMOR());

			VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
			if (request.getCpu() != null) {
				configSpec.setNumCPUs(request.getCpu());
			}
			if (request.getMemoryMb() != null) {
				configSpec.setMemoryMB(request.getMemoryMb());
			}

			// 네트워크 카드 재매핑
			VirtualDevice[] devices = template.getConfig().getHardware().getDevice();
			List<VirtualDeviceConfigSpec> deviceChanges = new ArrayList<>();

			for (VirtualDevice device : devices) {
				if (device instanceof VirtualEthernetCard nic) {
					VirtualEthernetCardNetworkBackingInfo backing = new VirtualEthernetCardNetworkBackingInfo();
					backing.setDeviceName(network.getName());
					backing.setNetwork(network.getMOR());

					nic.setBacking(backing);
					if (nic.getConnectable() != null) {
						nic.getConnectable().setStartConnected(true);
						nic.getConnectable().setConnected(true);
					}

					VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
					nicSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
					nicSpec.setDevice(nic);
					deviceChanges.add(nicSpec);
					break;
				}
			}

			if (!deviceChanges.isEmpty()) {
				configSpec.setDeviceChange(deviceChanges.toArray(new VirtualDeviceConfigSpec[0]));
			}

			VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
			cloneSpec.setLocation(relocateSpec);
			cloneSpec.setPowerOn(request.isPowerOn());
			cloneSpec.setTemplate(false);
			cloneSpec.setConfig(configSpec);

			// Linux guest customization - IP 고정 입력이 있을 때만
			if (request.getIpAddress() != null && !request.getIpAddress().isBlank()) {
				CustomizationFixedIp ip = new CustomizationFixedIp();
				ip.setIpAddress(request.getIpAddress());

				CustomizationIPSettings ipSettings = new CustomizationIPSettings();
				ipSettings.setIp(ip);
				ipSettings.setSubnetMask(request.getSubnetMask());

				if (request.getGateway() != null && !request.getGateway().isBlank()) {
					ipSettings.setGateway(new String[]{request.getGateway()});
				}
				if (request.getDnsServer() != null && !request.getDnsServer().isBlank()) {
					ipSettings.setDnsServerList(new String[]{request.getDnsServer()});
				}

				CustomizationAdapterMapping adapter = new CustomizationAdapterMapping();
				adapter.setAdapter(ipSettings);

				CustomizationLinuxPrep linuxPrep = new CustomizationLinuxPrep();
				linuxPrep.setDomain("localdomain");

				CustomizationFixedName fixedName = new CustomizationFixedName();
				fixedName.setName(
					(request.getHostName() != null && !request.getHostName().isBlank())
						? request.getHostName()
						: request.getVmName()
				);
				linuxPrep.setHostName(fixedName);

				CustomizationGlobalIPSettings global = new CustomizationGlobalIPSettings();
				if (request.getDnsServer() != null && !request.getDnsServer().isBlank()) {
					global.setDnsServerList(new String[]{request.getDnsServer()});
				}

				CustomizationSpec customizationSpec = new CustomizationSpec();
				customizationSpec.setIdentity(linuxPrep);
				customizationSpec.setGlobalIPSettings(global);
				customizationSpec.setNicSettingMap(new CustomizationAdapterMapping[]{adapter});

				cloneSpec.setCustomization(customizationSpec);
			}

			Task task = template.cloneVM_Task(vmFolder, request.getVmName(), cloneSpec);
			String taskStatus = task.waitForTask();

			result.put("success", Task.SUCCESS.equals(taskStatus));
			result.put("taskStatus", taskStatus);
			result.put("vmName", request.getVmName());
			result.put("cpu", request.getCpu());
			result.put("memoryMb", request.getMemoryMb());
			result.put("network", network.getName());
			result.put("datastore", datastore.getName());
			result.put("templateName", templateName);

			if (!Task.SUCCESS.equals(taskStatus)) {
				TaskInfo info = task.getTaskInfo();
				result.put("message", "VM 배포 실패");
				result.put("error", info != null && info.getError() != null
					? info.getError().getLocalizedMessage()
					: "알 수 없는 오류");
				return result;
			}

			// 배포 성공 후 생성된 VM 조회
			VirtualMachine createdVm = findVmByName(rootFolder, request.getVmName());

			String vmId = null;
			String guestIp = null;

			if (createdVm != null) {
				vmId = createdVm.getMOR().getVal();

				if (request.isPowerOn()) {
					guestIp = waitForGuestIp(createdVm, 10, 3000);
				}
			}
			String effectiveIp = guestIp;
			if ((effectiveIp == null || effectiveIp.isBlank())
				&& request.getIpAddress() != null
				&& !request.getIpAddress().isBlank()) {
				effectiveIp = request.getIpAddress();
			}
			Map<String, Object> remoteInfo = resolveRemoteConnectionInfo(request, guestIp);

			String remoteProtocol = (String) remoteInfo.get("remoteProtocol");
			Integer remotePort = (Integer) remoteInfo.get("remotePort");
			String remoteUsername = (String) remoteInfo.get("remoteUsername");
			Boolean guacReady = (Boolean) remoteInfo.get("guacReady");

			String quickConnectUri = buildQuickConnectUri(
				remoteProtocol,
				remoteUsername,
				guestIp,
				remotePort
			);

			String guacUrl = null;

			if (effectiveIp != null && !effectiveIp.isBlank()) {
				GuacConnectionCreateRequest guacReq = GuacConnectionCreateRequest.builder()
					.connectionName(request.getVmName())
					.protocol("ssh")
					.hostname(effectiveIp)
					.port("22")
					.username("fisa")
					.password("VMware1!")
					.build();

				GuacConnectionCreateResponse guacRes = guacamoleService.createConnection(guacReq);
				guacUrl = guacRes.getUrl();

				result.put("guacConnectionId", guacRes.getConnectionId());
				result.put("guacIdentifier", guacRes.getIdentifier());
			}

			result.put("ipAddress", effectiveIp);
			result.put("guacReady", guacUrl != null);
			result.put("guacUrl", guacUrl);
			result.put("message", "VM 배포 성공");
			result.put("vmId", vmId);
			result.put("remoteProtocol", remoteProtocol);
			result.put("remotePort", remotePort);
			result.put("remoteUsername", remoteUsername);
			return result;

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "VM 배포 실패");
			result.put("error", e.getClass().getSimpleName());
			result.put("detail", e.getMessage());
			return result;
		} finally {
			if (serviceInstance != null) {
				try {
					serviceInstance.getServerConnection().logout();
				} catch (Exception ignored) {
				}
			}
		}
	}
	private VirtualMachine findVmByName(Folder rootFolder, String vmName) throws Exception {
		return (VirtualMachine) new InventoryNavigator(rootFolder)
			.searchManagedEntity("VirtualMachine", vmName);
	}

	private String waitForGuestIp(VirtualMachine vm, int maxAttempts, long sleepMillis) throws Exception {
		for (int i = 0; i < maxAttempts; i++) {
			try {
				if (vm.getGuest() != null) {
					String ip = vm.getGuest().getIpAddress();
					if (ip != null && !ip.isBlank()) {
						return ip;
					}
				}
			} catch (Exception ignored) {
			}
			Thread.sleep(sleepMillis);
		}
		return null;
	}
	private String buildQuickConnectUri(String protocol, String username, String host, Integer port) {
		if (protocol == null || host == null || host.isBlank()) {
			return null;
		}

		StringBuilder uri = new StringBuilder();
		uri.append(protocol).append("://");

		if (username != null && !username.isBlank()) {
			uri.append(username).append("@");
		}

		uri.append(host);

		if (port != null) {
			uri.append(":").append(port);
		}

		if ("rdp".equalsIgnoreCase(protocol)) {
			uri.append("/?ignore-cert=true");
		} else {
			uri.append("/");
		}

		return uri.toString();
	}
	private Map<String, Object> resolveRemoteConnectionInfo(DeployVmRequest request, String guestIp) {
		Map<String, Object> info = new LinkedHashMap<>();

		String osType = request.getOsType() != null ? request.getOsType().toLowerCase() : "";

		if (osType.contains("windows")) {
			info.put("remoteProtocol", "rdp");
			info.put("remotePort", 3389);
			info.put("remoteUsername", "Administrator");
		} else {
			info.put("remoteProtocol", "ssh");
			info.put("remotePort", 22);
			info.put("remoteUsername", "rocky");
		}

		info.put("guacReady", guestIp != null && !guestIp.isBlank());
		return info;
	}

}