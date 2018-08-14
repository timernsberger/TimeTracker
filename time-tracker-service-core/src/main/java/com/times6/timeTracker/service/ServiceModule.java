package com.times6.timeTracker.service;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.times6.timeTracker.db.DatabaseModule;

public class ServiceModule extends AbstractModule {
	private static final String CONFIG_FILE = "config/serviceConfig.json";


	@Override
	protected void configure() {
		install(new ControllerModule());
		install(new DatabaseModule());
	}
	
	@Provides
	private ServiceConfig getConfig(ObjectMapper objectMapper) {
		try {
			return objectMapper.readValue(new File(CONFIG_FILE), ServiceConfig.class);
		} catch (IOException e) {
			throw new RuntimeException("failed to read config from " + CONFIG_FILE, e);
		}
	}
}
