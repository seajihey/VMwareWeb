package com.vmportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vcenter")
public class VCenterProperties {

	private String url;
	private String username;
	private String password;
	private String datacenter;
	private String cluster;
	private String resourcePool;
	private String network;
	private String datastore;
	private String rockyTemplate;

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getDatacenter() { return datacenter; }
	public void setDatacenter(String datacenter) { this.datacenter = datacenter; }

	public String getCluster() { return cluster; }
	public void setCluster(String cluster) { this.cluster = cluster; }

	public String getResourcePool() { return resourcePool; }
	public void setResourcePool(String resourcePool) { this.resourcePool = resourcePool; }

	public String getNetwork() { return network; }
	public void setNetwork(String network) { this.network = network; }

	public String getDatastore() { return datastore; }
	public void setDatastore(String datastore) { this.datastore = datastore; }

	public String getRockyTemplate() { return rockyTemplate; }
	public void setRockyTemplate(String rockyTemplate) { this.rockyTemplate = rockyTemplate; }
}