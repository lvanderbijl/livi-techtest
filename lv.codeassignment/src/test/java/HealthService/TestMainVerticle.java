package HealthService;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

	
  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void it_should_create_a_dummy_service(Vertx vertx, VertxTestContext testContext) throws Throwable {
	  var client = WebClient.wrap(vertx.createHttpClient());
	  client
	  	.get(8088,"localhost", "/services/one")
	  	.send()
	  	.onComplete(testContext.succeeding(resp -> testContext.verify(() -> {
	  		assertEquals("hello", resp.body().toString());
	  		testContext.completeNow();
	  	})))
	  	.onFailure(err -> {
	  		testContext.failNow(err);
	  	});
  }
}