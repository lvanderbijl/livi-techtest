package com.lv.codeassignments.kry.serviceMonitor.services;

import java.util.List;

import com.lv.codeassignments.kry.serviceMonitor.monitors.MonitorPollResult;
import com.lv.codeassignments.kry.serviceMonitor.monitors.ServiceDetails;

import io.vertx.core.Future;

public interface IDataService {
	Future<Integer> savePollResult(MonitorPollResult result);

	Future<List<MonitorPollResult>> getPollResults(int monitorId);

	Future<List<ServiceDetails>> getServiceDetails();

	Future<Integer> createServiceDetails(String serviceName, ServiceDetails details);

	Future<Void> deleteServiceMonitor(int monitorId);

	Future<Void> deletePollResult(int pollId);

}
