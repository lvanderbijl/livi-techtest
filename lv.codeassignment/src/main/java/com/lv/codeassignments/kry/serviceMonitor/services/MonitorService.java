package com.lv.codeassignments.kry.serviceMonitor.services;

import java.util.ArrayList;
import java.util.List;

import com.lv.codeassignments.kry.serviceMonitor.monitors.MonitorPollResult;
import com.lv.codeassignments.kry.serviceMonitor.monitors.ServiceMonitor;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

public class MonitorService {
	private List<ServiceMonitor> monitors;
	// Storing vertx here to allow for creating periods for specific monitors
	// Not sure I like it here....
	private Vertx vertx;
	private long defaultPollingPeriod;
	private WebClient webClient;
	private IDataService dataService;

	public MonitorService(Vertx vertx, IDataService dataService, long defaultPollingPeriod) {
		this.vertx = vertx;
		monitors = new ArrayList<ServiceMonitor>();
		this.defaultPollingPeriod = defaultPollingPeriod;
		this.webClient = WebClient.wrap(vertx.createHttpClient());
	}

	// TODO: we need a load method here to load the existing monitors from the db on
	// startup

	// TODO make this private and enforce db creation of monitor before adding it to
	// the list
	// Replace this with a public "createNewMonitor" method that first saves to db
	// and then loads it
	public void addMonitor(ServiceMonitor monitor) {
		addMonitor(monitor, defaultPollingPeriod);
	}

	/*
	 * public Future<ServiceMonitor> createMonitor(String serviceName,
	 * ServiceDetails details) { return dataService. }
	 */

	public void addMonitor(ServiceMonitor monitor, long pollingSchedule) {
		this.monitors.add(monitor);
		vertx.setPeriodic(pollingSchedule, registerPolling(monitor.getServiceName()));
	}

	private Handler<Long> registerPolling(String serviceName) {
		var monitor = monitors.stream().filter(a -> a.getServiceName() == serviceName).findFirst();
		return l -> {
			if (monitor.isPresent()) {
				monitor.get().pollService(webClient, handlePollResult());
			}
		};
	}

	protected Handler<MonitorPollResult> handlePollResult() {
		return pollResult -> {
			dataService.savePollResult(pollResult);
		};
	}

	public List<ServiceMonitor> getMonitors() {
		return this.monitors;
	}

	// TODO: Don't forget an onClose method to flush any calls to database

}
