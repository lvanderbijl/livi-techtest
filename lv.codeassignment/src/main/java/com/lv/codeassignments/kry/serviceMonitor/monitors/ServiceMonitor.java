package com.lv.codeassignments.kry.serviceMonitor.monitors;

import java.time.LocalDateTime;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;

public class ServiceMonitor {

	private ServiceDetails serviceDetails;

	public ServiceMonitor(ServiceDetails srvcDetails) {

		this.serviceDetails = srvcDetails;
	}

	public Future<Void> pollService(WebClient webClient, Handler<MonitorPollResult> pollHandler) {
		Promise<Void> promise = Promise.promise();
		webClient.get(serviceDetails.getPort(), serviceDetails.getDomain(), serviceDetails.getPath()).send()
				.onSuccess((resp) -> {
					MonitorStatus monitorStatus;
					if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
						monitorStatus = MonitorStatus.OK;
					} else {
						monitorStatus = MonitorStatus.FAIL;
					}
					// Yes this should probably be done with composition rather than nested
					var poll = new MonitorPollResult();
					poll.monitorId = serviceDetails.getMonitorId();
					poll.monitorDate = LocalDateTime.now();
					poll.status = monitorStatus;
					poll.url = serviceDetails.getUrl();
					pollHandler.handle(poll);
					promise.complete();

				}).onFailure(exc -> {
					promise.fail(exc);
				});
		return promise.future();
	}

	public String getServiceName() {
		return this.serviceDetails.getServiceName();
	}

	public void setServiceName(String serviceName) {
		this.serviceDetails.setServiceName(serviceName);
	}

}
