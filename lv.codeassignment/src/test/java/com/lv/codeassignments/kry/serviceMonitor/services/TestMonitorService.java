package com.lv.codeassignments.kry.serviceMonitor.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.lv.codeassignments.kry.serviceMonitor.MainVerticle;
import com.lv.codeassignments.kry.serviceMonitor.monitors.MonitorPollResult;
import com.lv.codeassignments.kry.serviceMonitor.monitors.ServiceDetails;
import com.lv.codeassignments.kry.serviceMonitor.monitors.ServiceMonitor;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class TestMonitorService {

	private WebClient client;
	private IDataService dataService;

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
		client = WebClient.wrap(vertx.createHttpClient());
		dataService = new DataService();
	}

	@Test
	void it_should_add_a_named_service_monitor(Vertx vertx, VertxTestContext testContext) {
		var service = new FakeMonitorService(vertx, null, 10000);
		service.addMonitor(new ServiceMonitor(getServiceDetails()));
		assertTrue(service.getMonitors().size() == 1);
		testContext.completeNow();
	}

	@Test
	void it_should_poll_the_service_monitors(Vertx vertx, VertxTestContext testContext) {
		var service = new FakeMonitorService(vertx, null, 10000);
		service.addMonitor(new ServiceMonitor(getServiceDetails()), 100);
		vertx.setTimer(1000, l -> {
			testContext.verify(() -> {
				assertEquals(9, service.results.size());
				testContext.completeNow();
			});

		});

	}

	/*
	 * @Test void it_should_create_a_new_service_in_the_database(Vertx vertx,
	 * VertxTestContext testContext) { var service = new MonitorService(vertx,
	 * 10000); service.createMonitor("a brand new monitor", getServiceDetails())
	 * .onComplete(testContext.succeeding(ar -> testContext.verify(() -> {
	 * assertEquals(1, service.getMonitor("a brand new monitor").getMonitorId());
	 * })));
	 * 
	 * }
	 */

	private ServiceDetails getServiceDetails() {
		var currTime = LocalDateTime.now();
		return new ServiceDetails(123, "My service", "localhost", 8088, "services/one", currTime, currTime);
	}

	class FakeMonitorService extends MonitorService {
		public List<MonitorPollResult> results = new ArrayList<MonitorPollResult>();
		public boolean handlerWasCalled = false;

		public FakeMonitorService(Vertx vertx, IDataService dataService, long defaultPollingPeriod) {
			super(vertx, dataService, defaultPollingPeriod);

		}

		protected Handler<MonitorPollResult> handlePollResult() {
			return pollResult -> {
				results.add(pollResult);
				handlerWasCalled = true;
			};
		}
	}

}
