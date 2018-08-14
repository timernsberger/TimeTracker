package com.times6.timeTracker;
import java.nio.file.Paths;

import javax.inject.Inject;

import com.times6.timeTracker.service.ServiceConfig;
import com.times6.timeTracker.service.ServiceModule;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class Main {
	public static void main(String[] args) throws Exception {
		final Injector injector = Guice.createInjector(new ServiceModule());
		injector.getInstance(Main.class).run();
	}
	
	private Injector injector;

	@Inject
	public Main(Injector injector) {
		this.injector = injector;
	}

	public void run() throws Exception {
		ServiceConfig serviceConfig = injector.getInstance(ServiceConfig.class);
		
		final int port = serviceConfig.getPort();
		final Server server = new Server(port);
		
		ServletContextHandler servletHandler = new ServletContextHandler();
		servletHandler.setContextPath("/api");
		servletHandler.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));
		ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
		servletHandler.addServlet(sh, "/*");

		HandlerList handlers = new HandlerList(servletHandler);
		
		if(serviceConfig.isUseStaticAssets()) {
			ServletContextHandler staticHandler = new ServletContextHandler();
			ServletHolder staticAssetHolder = new ServletHolder(DefaultServlet.class);
			staticAssetHolder.setInitParameter("resourceBase", Paths.get(".", "static").toString());
			staticAssetHolder.setInitParameter("dirAllowed", String.valueOf(false));
			staticAssetHolder.setInitParameter("precompressed", String.valueOf(true));
			staticHandler.addServlet(staticAssetHolder, "/");

			handlers.addHandler(staticHandler);
		} else {
			ServletContextHandler proxyHandler = new ServletContextHandler();
			proxyHandler.setContextPath("/");
			ServletHolder appProxy = new ServletHolder(ProxyServlet.Transparent.class);
			appProxy.setInitParameter("proxyTo", "http://localhost:" + (port + 1));
			proxyHandler.addServlet(appProxy, "/*");

			handlers.addHandler(proxyHandler);
		}

		server.setHandler(handlers);
		
		server.start();
		server.join();
	}
}
