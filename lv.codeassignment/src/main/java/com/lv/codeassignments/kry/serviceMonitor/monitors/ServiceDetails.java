package com.lv.codeassignments.kry.serviceMonitor.monitors;

import java.time.LocalDateTime;

public class ServiceDetails {

	private int monitorId;
	private String serviceName;
	private LocalDateTime createdDate;
	private LocalDateTime modifiedDate;
	private String domain;
	private int port;
	private String path;

	public ServiceDetails(String serviceName, String domain, int port, String path) {
		this(0, serviceName, domain, port, path, LocalDateTime.now(), LocalDateTime.now());
	}

	public ServiceDetails(int monitorId, String serviceName, String domain, int port, String path,
			LocalDateTime createdDate, LocalDateTime modifiedDate) {
		this.monitorId = monitorId;
		this.serviceName = serviceName;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.domain = domain;
		this.port = port;
		this.path = path;
	}

	public int getMonitorId() {
		return monitorId;
	}

	public String getDomain() {
		return domain;
	}

	public int getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
		this.modifiedDate = LocalDateTime.now();
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public LocalDateTime getModifiedDate() {
		return modifiedDate;
	}

	public String getUrl() {
		return new StringBuilder("http://").append(domain).append(":").append(port).append("/").append(path).toString();
	}
}
