package com.lv.codassignments.kry.serviceMonitor.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.lv.codeassignments.kry.serviceMonitor.MainVerticle;
import com.lv.codeassignments.kry.serviceMonitor.routing.HealthServiceRouting;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestHealthServiceRouting {

	static Stream<Arguments> serviceNames() {
		return Stream.of(Arguments.of("one"), Arguments.of("t"), Arguments.of("three/four"));
	}

	static Stream<Arguments> statusOverrides() {
		return Stream.of(Arguments.of("one", 500), Arguments.of("t", 201), Arguments.of("three/four", 301));
	}

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
	}

	@ParameterizedTest
	@MethodSource("serviceNames")
	void it_should_create_service_endpoints(String srvcName, Vertx vertx, VertxTestContext testContext)
			throws Throwable {
		var client = WebClient.wrap(vertx.createHttpClient());
		client.get(8088, "localhost", String.format("/services/{0}", srvcName)).send()
				.onComplete(testContext.succeeding(resp -> testContext.verify(() -> {
					assertNotEquals(404, resp.statusCode());
					testContext.completeNow();
				}))).onFailure(err -> {
					testContext.failNow(err);
				});
	}

	@Test
	void it_should_return_404_for_unknown_route(Vertx vertx, VertxTestContext testContext) throws Throwable {
		var client = WebClient.wrap(vertx.createHttpClient());
		client.get(8088, "localhost", String.format("/servicex/one")).send()
				.onComplete(testContext.succeeding(resp -> testContext.verify(() -> {
					assertEquals(404, resp.statusCode());
					testContext.completeNow();
				}))).onFailure(err -> {
					testContext.failNow(err);
				});
	}

	@RepeatedTest(20)
	void it_should_only_return_201_or_500_status(Vertx vertx, VertxTestContext testContext) throws Throwable {
		var client = WebClient.wrap(vertx.createHttpClient());
		client.get(8088, "localhost", String.format("/services/one")).send()
				.onComplete(testContext.succeeding(resp -> testContext.verify(() -> {
					var validCodes = Arrays.asList(201, 500);
					assertTrue(validCodes.contains(resp.statusCode()));
					testContext.completeNow();
				}))).onFailure(err -> {
					testContext.failNow(err);
				});
	}

	@ParameterizedTest
	@MethodSource("statusOverrides")
	void it_should_create_override_the_return_status_code(String srvcName, int statusCode, Vertx vertx,
			VertxTestContext testContext) throws Throwable {
		var client = WebClient.wrap(vertx.createHttpClient());
		var path = String.format("/services/{0}");
		// yes - should test for invalid status codes.... but it's a fake service
		client.get(8088, "localhost", path)
				.addQueryParam(HealthServiceRouting.QUERY_PARAM_FORCE_STATUS_CODE, String.valueOf(statusCode)).send()
				.onComplete(testContext.succeeding(resp -> testContext.verify(() -> {
					assertEquals(statusCode, resp.statusCode());
					testContext.completeNow();
				}))).onFailure(err -> {
					testContext.failNow(err);
				});
	}

}
