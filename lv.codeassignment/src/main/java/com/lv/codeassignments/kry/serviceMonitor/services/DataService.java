package com.lv.codeassignments.kry.serviceMonitor.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.lv.codeassignments.kry.serviceMonitor.monitors.MonitorPollResult;
import com.lv.codeassignments.kry.serviceMonitor.monitors.MonitorStatus;
import com.lv.codeassignments.kry.serviceMonitor.monitors.ServiceDetails;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;

public class DataService implements IDataService {

	private final static String SCHEMA_NAME = "livitechtest";
	private final static String TABLE_POLL_RESULTS = "PollResults";
	private final static String TABLE_SERVICE_MONITORS = "ServiceMonitors";

	private PgPool client;

	public DataService() {
		// Yeah this should all be in configuration and pws should be stored in key
		// store not code
		PgConnectOptions connectOptions = new PgConnectOptions().setPort(5432).setHost("127.0.0.1")
				.setDatabase("leandervanderbijl1").setUser("postgres").setPassword("");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		client = PgPool.pool(connectOptions, poolOptions);

	}

	public Future<Integer> savePollResult(MonitorPollResult result) {
		Promise<Integer> promise = Promise.promise();
		// TODO: pull these out into a sql generator class
		var query = "INSERT INTO %s.%s (monitor_id, monitor_url, monitor_status, poll_date) VALUES ($1, $2, $3, $4) RETURNING id"
				.formatted(SCHEMA_NAME, TABLE_POLL_RESULTS);

		client.preparedQuery(query).execute(Tuple.of(result.monitorId, result.url, result.status, result.monitorDate))
				.onComplete(ar -> {
					if (ar.succeeded()) {
						int id = -1;
						var rows = ar.result();
						for (var row : rows) {
							id = row.getInteger(0);
						}

						if (rows.size() != 1 || id == -1) {
							// TODO: ideally we'd test this with mock db clients skipping this due to time
							// constraints
							promise.fail(ar.cause());
						}
						promise.complete(id);
					} else {
						promise.fail(ar.cause());
					}
				});
		return promise.future();
	}

	public Future<List<MonitorPollResult>> getPollResults(int monitorId) {
		Promise<List<MonitorPollResult>> promise = Promise.promise();

		client.query("SELECT * FROM %s.%s where monitor_id = %s".formatted(SCHEMA_NAME, TABLE_POLL_RESULTS, monitorId))
				.execute(ar -> {
					if (ar.succeeded()) {
						// TODO: This can be refactored out to a serialization class
						var results = new ArrayList<MonitorPollResult>();
						var rows = ar.result();
						for (var row : rows) {
							var monitor = new MonitorPollResult() {
								{
									monitorId = row.getInteger("monitor_id");
									monitorDate = row.getLocalDateTime("poll_date");
									status = MonitorStatus.valueOf(row.getString("monitor_status"));
									url = row.getString("monitor_url");
								}
							};
							results.add(monitor);
						}
						promise.complete(results);
					} else {
						promise.fail(ar.cause());
					}
				});
		return promise.future();
	}

	public Future<List<ServiceDetails>> getServiceDetails() {
		Promise<List<ServiceDetails>> promise = Promise.promise();

		client.query("SELECT * FROM %s.%s".formatted(SCHEMA_NAME, TABLE_SERVICE_MONITORS)).execute(ar -> {
			if (ar.succeeded()) {
				var results = new ArrayList<ServiceDetails>();
				var rows = ar.result();
				for (var row : rows) {
					int id = row.getInteger("id");
					var name = row.getString("monitor_name");
					var domain = row.getString("url_domain");
					int port = row.getInteger("url_port");
					var path = row.getString("url_path");
					var create = row.getLocalDateTime("created_date");
					var mod = row.getLocalDateTime("modified_date");
					var dets = new ServiceDetails(id, name, domain, port, path, create, mod);
					results.add(dets);
				}
				promise.complete(results);
			} else {
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}

	public Future<Integer> createServiceDetails(String serviceName, ServiceDetails details) {
		Promise<Integer> promise = Promise.promise();
		var query = "INSERT INTO %s.%s (monitor_name, url_port, url_path, url_domain, created_date, modified_date) VALUES ($1, $2, $3, $4, $5, $6) RETURNING id;"
				.formatted(SCHEMA_NAME, TABLE_SERVICE_MONITORS);
		var curTime = LocalDateTime.now();
		client.preparedQuery(query).execute(
				Tuple.of(serviceName, details.getPort(), details.getPath(), details.getDomain(), curTime, curTime))
				.onComplete(ar -> {
					if (ar.succeeded()) {

						int id = -1;
						for (var row : ar.result()) {
							id = row.getInteger(0);
						}

						if (id == -1) {
							// TODO: ideally we'd test this with mock db clients skipping this due to time
							// constraints
							promise.fail("couldn't find id");
						} else {
							promise.complete(id);
						}
					} else {
						promise.fail(ar.cause());
					}
				});
		return promise.future();
	}

	public Future<Void> deleteServiceMonitor(int monitorId) {
		return deleteRowFromTable(TABLE_SERVICE_MONITORS, monitorId);
	}

	public Future<Void> deletePollResult(int pollId) {
		return deleteRowFromTable(TABLE_POLL_RESULTS, pollId);
	}

	private Future<Void> deleteRowFromTable(String tableName, int id) {
		Promise<Void> promise = Promise.promise();
		var query = "DELETE FROM %s.%s WHERE id = %s;".formatted(SCHEMA_NAME, tableName, id);
		client.preparedQuery(query).execute().onComplete(ar -> {
			if (ar.succeeded()) {
				promise.complete();
			} else {
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}

	public Future<Void> close() {
		return this.client.close();
	}

}
