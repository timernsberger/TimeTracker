package com.times6.timeTracker.db.dynamo;

import javax.inject.Inject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.times6.timeTracker.db.DatabaseModule;

public class DatabaseCreator {

	private AmazonDynamoDB dynamo;
	
	@Inject
	public DatabaseCreator(AmazonDynamoDB dynamo) {
		this.dynamo = dynamo;
	}
	
	public void createTables() {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamo);
		CreateTableRequest taskTypeRequest = mapper.generateCreateTableRequest(TaskTypeRecord.class);
        taskTypeRequest.withProvisionedThroughput(new ProvisionedThroughput(5L, 1L));
		dynamo.createTable(taskTypeRequest);

		CreateTableRequest taskRequest = mapper.generateCreateTableRequest(TaskRecord.class);
        taskRequest.withProvisionedThroughput(new ProvisionedThroughput(5L, 1L));
        dynamo.createTable(taskRequest);
	}
	
	public static void main(String[] args) {
		// needs security token
		final Injector injector = Guice.createInjector(new DatabaseModule());
		DatabaseCreator creator = injector.getInstance(DatabaseCreator.class);
		creator.createTables();
	}
}
