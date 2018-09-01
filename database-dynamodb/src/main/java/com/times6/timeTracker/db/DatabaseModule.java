package com.times6.timeTracker.db;
import java.io.File;
import java.io.IOException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.times6.timeTracker.db.dynamo.DatabaseConfig;
import com.times6.timeTracker.db.dynamo.DynamoTaskDao;
import com.times6.timeTracker.db.dynamo.DynamoTaskTypeDao;


public class DatabaseModule extends AbstractModule {
	private static final String CONFIG_FILE = "config/databaseConfig.json";

	@Override
	protected void configure() {
		bind(TaskDao.class).to(DynamoTaskDao.class);
		bind(TaskTypeDao.class).to(DynamoTaskTypeDao.class);
	}

	@Provides
	private AmazonDynamoDB getDynamoClient(DatabaseConfig config) {
		return AmazonDynamoDBClient.builder()
				.withRegion(Regions.fromName(config.getRegion()))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(config.getAccessKey(), config.getSecret())))
				.build();
	}

	@Provides
	private DatabaseConfig getConfig(ObjectMapper objectMapper) {
		try {
			return objectMapper.readValue(new File(CONFIG_FILE), DatabaseConfig.class);
		} catch (IOException e) {
			throw new RuntimeException("failed to read config from " + CONFIG_FILE, e);
		}
	}
}