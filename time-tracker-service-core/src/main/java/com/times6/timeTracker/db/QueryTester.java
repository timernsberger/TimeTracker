package com.times6.timeTracker.db;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class QueryTester {

	public static void main(String[] args) {
		final Injector injector = Guice.createInjector(new DatabaseModule());
		
		TaskDao dao = injector.getInstance(TaskDao.class);
		System.out.println(dao.getLatest("1"));
	}
	
}
