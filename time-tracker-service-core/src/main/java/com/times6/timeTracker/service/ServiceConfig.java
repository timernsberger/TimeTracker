package com.times6.timeTracker.service;

import lombok.Data;

@Data
public class ServiceConfig {
	private boolean useStaticAssets;
	private boolean development;
	private String defaultUserId;
	private int port;
}
