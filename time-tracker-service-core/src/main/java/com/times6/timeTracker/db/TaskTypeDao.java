package com.times6.timeTracker.db;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.collect.ImmutableMap;

public class TaskTypeDao {

	private DynamoDBMapper mapper;
	
	@Inject
	public TaskTypeDao(AmazonDynamoDB dynamo) {
		mapper = new DynamoDBMapper(dynamo);
	}
	
	public Collection<TaskTypeRecord> getAll(String userId) {
		DynamoDBQueryExpression<TaskTypeRecord> queryExpression = new DynamoDBQueryExpression<TaskTypeRecord>()
				.withKeyConditionExpression("userId = :userId")
				.withExpressionAttributeValues(ImmutableMap.of(
						":userId", new AttributeValue().withS(userId)));
		PaginatedQueryList<TaskTypeRecord> tasks = mapper.query(TaskTypeRecord.class, queryExpression);
		return new ArrayList<>(tasks);
	}
	
	public void save(TaskTypeRecord record) {
		mapper.save(record);
	}

	public void remove(TaskTypeRecord record) {
		mapper.delete(record);
	}
}
