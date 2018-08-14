package com.times6.timeTracker.service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class OidcHeaderFilter implements ContainerRequestFilter {
    public static final String USER_ID_HEADER_NAME = "OIDC_CLAIM_email";
	private ServiceConfig serviceConfig;
	private HttpServletRequest request;
	
	@Inject
	public OidcHeaderFilter(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
	}

	@Override
	public void filter(ContainerRequestContext context) {
		String userEmail = context.getHeaderString(USER_ID_HEADER_NAME);
		if(userEmail == null) {
			if(serviceConfig.isDevelopment() && serviceConfig.getDefaultUserId() != null) {
				log.info("no {} specified; falling back to \"{}\"", USER_ID_HEADER_NAME, serviceConfig.getDefaultUserId());
				userEmail = serviceConfig.getDefaultUserId();
			} else {
				log.info("no {} specified; rejecting request", USER_ID_HEADER_NAME);
				context.abortWith(Response.status(Status.UNAUTHORIZED).build());
			}
		}
		request.setAttribute("userId", userEmail);
	}

    @Context
	public void setRequest(HttpServletRequest request) {
	    this.request = request;
    }
}
