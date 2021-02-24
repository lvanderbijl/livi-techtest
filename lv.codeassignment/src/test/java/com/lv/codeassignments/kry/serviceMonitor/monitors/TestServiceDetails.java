package com.lv.codeassignments.kry.serviceMonitor.monitors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.lv.codeassignments.kry.serviceMonitor.MainVerticle;
import com.lv.codeassignments.kry.serviceMonitor.services.DataService;
import com.lv.codeassignments.kry.serviceMonitor.services.IDataService;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class TestServiceDetails {

	private WebClient client;
	private IDataService dataService;

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
		client = WebClient.wrap(vertx.createHttpClient());
		dataService = new DataService();
	}

	@Test
	void it_should_log_the_create_date(Vertx vertx, VertxTestContext testContext) {
		var details = getServiceDetails(Optional.of(LocalDateTime.now()));
		var cal = details.getCreatedDate();
		assertEquals(Calendar.getInstance().get(Calendar.DATE), cal.getDayOfMonth());
		testContext.completeNow();
	}

	@Test
	void it_should_log_the_modified_date(Vertx vertx, VertxTestContext testContext) {
		var currTime = LocalDateTime.now();
		var details = new ServiceDetails(123, "My Service", "localhost", 8088, "services/sample", currTime, currTime);
		var createdCal = details.getCreatedDate();
		var origMod = details.getModifiedDate();
		assertEquals(createdCal, origMod);

		testContext.verify(() -> {
			details.setServiceName("newName");
			var mod = details.getModifiedDate();
			assertNotEquals(createdCal, mod);
			testContext.completeNow();
		});
	}

	private ServiceDetails getServiceDetails(Optional<LocalDateTime> time) {
		var currTime = time.orElse(LocalDateTime.now());
		return new ServiceDetails(223, "My Service", "localhost", 8088, "services/sample", currTime, currTime);
	}
}
