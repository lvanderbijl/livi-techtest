package com.lv.codassignments.kry.serviceMonitor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.lv.codeassignments.kry.serviceMonitor.MainVerticle;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
	}

	@Test
	void it_should_create_an_api(Vertx vertx, VertxTestContext testContext) throws Throwable {
		var deployments = vertx.deploymentIDs();
		assertTrue(deployments.size() > 0);
		testContext.completeNow();
	}

}
