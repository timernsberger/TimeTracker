package com.times6.timeTracker.service;
import com.google.inject.AbstractModule;

public class ControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TaskController.class);
		bind(TaskTypeController.class);
		bind(RequestLoggerFilter.class);
		bind(OidcHeaderFilter.class);
		install(new JacksonModule());	
	}
}
