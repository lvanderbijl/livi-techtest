package com.lv.codeassignments.kry.serviceMonitor.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.lv.codeassignments.kry.serviceMonitor.MainVerticle;
import com.lv.codeassignments.kry.serviceMonitor.monitors.MonitorPollResult;
import com.lv.codeassignments.kry.serviceMonitor.monitors.MonitorStatus;
import com.lv.codeassignments.kry.serviceMonitor.monitors.ServiceDetails;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

/**
 * THIS CLASS WRITES TO THE DATABASE - Ideally we'd mock the underlying sql
 * client instead, but I ran out of time to mock it
 * 
 * @author leandervanderbijl1
 *
 */
@ExtendWith(VertxExtension.class)
class TestDataService {

	private IDataService dataService;
	private int pollResultToDelete = -1;
	private int serviceMonitorToDelete = -1;

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
		dataService = new DataService();
	}

	@AfterEach
	void deleteRecords(Vertx vertx, VertxTestContext testContext) {
		if (serviceMonitorToDelete > -1) {
			dataService.deleteServiceMonitor(serviceMonitorToDelete).onComplete(ar -> {
				testContext.verify(() -> testContext.completeNow());
			});

		} else if (pollResultToDelete > -1) {
			dataService.deletePollResult(pollResultToDelete).onComplete(ar -> {
				testContext.verify(() -> testContext.completeNow());
			});
		} else {
			testContext.completeNow();
		}

	}

	@Test
	void it_should_connect_to_the_database(Vertx vertx, VertxTestContext testContext) {
		var client = getClient();

		client.query("SELECT * FROM livitechtest.PollResults").execute(ar -> {
			testContext.verify(() -> {
				if (ar.succeeded()) {
					RowSet<Row> result = ar.result();
					testContext.completeNow();
				} else {
					testContext.failNow(ar.cause());
				}

				client.close();
				testContext.completeNow();
			});

		});
	}

	@Test
	void it_should_retrieve_poll_results_for_a_monitor_id(Vertx vertx, VertxTestContext testContext) {

		dataService.getPollResults(1).onComplete(testContext.succeeding(results -> testContext.verify(() -> {
			assertTrue(results.size() > 0);
			testContext.completeNow();
		})));
	}

	@Test
	void it_should_retrieve_all_monitors(Vertx vertx, VertxTestContext testContext) {

		dataService.getServiceDetails().onComplete(testContext.succeeding(results -> testContext.verify(() -> {
			assertTrue(results.size() > 0);
			testContext.completeNow();
		})));
	}

	@Test
	void it_should_insert_a_poll_result(Vertx vertx, VertxTestContext testContext) {
		var dataService = new DataService();
		var result = new MonitorPollResult() {
			{
				monitorId = 1;
				monitorDate = LocalDateTime.now();
				url = "someurl";
				status = MonitorStatus.FAIL;
			}
		};

		dataService.savePollResult(result).onComplete(testContext.succeeding(id -> testContext.verify(() -> {
			assertTrue(id > -1);
			pollResultToDelete = id;
			testContext.completeNow();
		})));
	}

	@Test
	void it_should_insert_a_service_monitor(Vertx vertx, VertxTestContext testContext) {
		var dataService = new DataService();
		dataService.createServiceDetails("testMonitor", getServiceDetails()).onComplete(ar -> {
			if (ar.succeeded()) {
				assertTrue(ar.result() > -1);
				serviceMonitorToDelete = ar.result();
				testContext.completeNow();
			}
		});
	}

	@Test
	void it_should_delete_a_poll_result(Vertx vertx, VertxTestContext testContext) {
		var dataService = new DataService();
		// This is a bad test cause I've hardcoded id one - and it delete it the first
		// time it ran
		// this should be fixed using mock db clients
		// but we're using deletes as part of after each, so we
		// should be ok for this purpose
		dataService.deletePollResult(1).onComplete(ar -> {
			if (ar.succeeded()) {
				testContext.completeNow();
			}
		});
	}

	@Test
	void it_should_delete_a_service_monitor(Vertx vertx, VertxTestContext testContext) {
		var dataService = new DataService();
		// This is a bad test cause I've hardcoded id one - and it delete it the first
		// time it ran
		// this should be fixed using mock db clients
		// but we're using deletes as part of after each, so we
		// should be ok for this purpose
		dataService.deleteServiceMonitor(1).onComplete(ar -> {
			if (ar.succeeded()) {
				testContext.completeNow();
			}
		});
	}

	private PgPool getClient() {
		PgConnectOptions connectOptions = new PgConnectOptions().setPort(5432).setHost("127.0.0.1")
				.setDatabase("leandervanderbijl1").setUser("postgres").setPassword("");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		PgPool client = PgPool.pool(connectOptions, poolOptions);
		return client;
	}

	private ServiceDetails getServiceDetails() {
		var currTime = LocalDateTime.now();
		return new ServiceDetails(123, "My Name", "localhost", 8088, "services/sample", currTime, currTime);
	}

}
