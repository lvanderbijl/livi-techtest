package com.lv.codeassignments.kry.serviceMonitor.routing;

import java.util.Random;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class HealthServiceRouting {

	public final static String QUERY_PARAM_FORCE_STATUS_CODE = "forceStatusCode";

	public static Handler<RoutingContext> HandleFakeService() {
		// use the force status code if it's in the query params else use random logic
		// to generate a 201 or 500 response
		// Also ignoring validation for valid status codes
		// Yep.... UGLY but it's a fake service
		return (rc) -> {
			int statusCode;
			if (rc.queryParams().contains(HealthServiceRouting.QUERY_PARAM_FORCE_STATUS_CODE)) {
				var qp = rc.queryParam(HealthServiceRouting.QUERY_PARAM_FORCE_STATUS_CODE).get(0);
				statusCode = Integer.parseInt(qp);
			} else {
				statusCode = new Random().nextInt() % 2 == 1 ? 201 : 500;
			}
			rc.response().setStatusCode(statusCode).end();
		};
	}
}
