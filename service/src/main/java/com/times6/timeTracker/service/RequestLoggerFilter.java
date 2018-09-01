package com.times6.timeTracker.service;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class RequestLoggerFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		log.info("received request at " + context.getDate() + " with headers " + context.getHeaders());
	}

}
