package com.lv.codassignments.kry.serviceMonitor.monitors;

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

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class TestServiceMonitor {

	private WebClient client;

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
		client = WebClient.wrap(vertx.createHttpClient());
	}

	@Test
	void it_should_monitor_a_service(Vertx vertx, VertxTestContext testContext) {
		var startTime = LocalDateTime.now();
		var monitor = new ServiceMonitor(getServiceDetails());

		List<MonitorPollResult> results = new ArrayList<MonitorPollResult>();
		monitor.pollService(client, pollResult -> {
			results.add(pollResult);
		}).onComplete(Void -> {

			testContext.verify(() -> {
				assertEquals(1, results.size());
				var result = results.get(0);
				assertEquals(122, result.monitorId);
				assertTrue(startTime.isBefore(result.monitorDate));
				testContext.completeNow();
			});

		});

	}

	// I should be mocking the database here - normally I would do that using a di
	// service with a fake data service.
	// I ran out of time to do this properly... This test may fail depending on what
	// log it gets
	@Test
	void it_should_log_the_result_date_and_url(Vertx vertx, VertxTestContext testContext) {
		var monitor = new ServiceMonitor(getServiceDetails());
		List<MonitorPollResult> results = new ArrayList<MonitorPollResult>();
		monitor.pollService(client, pollResult -> {
			results.add(pollResult);
		}).onComplete(ar -> {
			testContext.verify(() -> {
				var log = results.get(0);
				assertTrue(log.monitorDate != null);
				assertTrue(log.status != null);
				assertTrue(log.url.length() > 0);
				assertEquals("http://localhost:8088/services/sample", log.url);
				testContext.completeNow();
			});
		});
	}

	@Test
	void it_should_set_a_new_service_name(Vertx vertx, VertxTestContext testContext) {
		var monitor = new ServiceMonitor(getServiceDetails());
		assertEquals("My Service", monitor.getServiceName());
		monitor.setServiceName("newName");
		assertEquals("newName", monitor.getServiceName());
		testContext.completeNow();
	}

	private ServiceDetails getServiceDetails() {
		var currTime = LocalDateTime.now();
		return new ServiceDetails(122, "My Service", "localhost", 8088, "services/sample", currTime, currTime);
	}
}
