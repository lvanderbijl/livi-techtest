package com.lv.codeassignments.kry.serviceMonitor;

import com.lv.codeassignments.kry.serviceMonitor.routing.HealthServiceRouting;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.RouterBuilder;

public class MainVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		// setup
		RouterBuilder.create(vertx, "src/main/resources/healthServices.yaml").onComplete(ar -> {
			if (ar.succeeded()) {
				RouterBuilder routerBuilder = ar.result();
				routerBuilder.operation("fakeService").handler(HealthServiceRouting.HandleFakeService());
				// bind further routes here
				startServer(routerBuilder.createRouter(), startPromise);
			} else {
				// route building didn't work
				Throwable exception = ar.cause();
				startPromise.fail(exception);
			}
		}).onFailure((exception) -> {
			startPromise.fail(exception);
		});

	}

	private void startServer(Router router, Promise<Void> startPromise) {
		vertx.createHttpServer().requestHandler(router).listen(8088, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				System.out.println("HTTP server started on port 8088");
			} else {
				startPromise.fail(http.cause());
			}
		});
	}

}
