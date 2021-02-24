package com.lv.codeassignments.kry.serviceMonitor.monitors;

import java.time.LocalDateTime;

/***
 * DTO for monitor logs
 */
public class MonitorPollResult {
	public int monitorId;
	public LocalDateTime monitorDate;
	public MonitorStatus status;
	public String url;
}
