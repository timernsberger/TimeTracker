package com.times6.timeTracker.db.dynamo;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.collect.ImmutableMap;
import com.times6.timeTracker.TaskType;
import com.times6.timeTracker.db.TaskTypeDao;

public class DynamoTaskTypeDao implements TaskTypeDao {

	private DynamoDBMapper mapper;
	
	@Inject
	public DynamoTaskTypeDao(AmazonDynamoDB dynamo) {
		mapper = new DynamoDBMapper(dynamo);
	}

	@Override
	public Collection<TaskType> getAll(String userId) {
		DynamoDBQueryExpression<TaskTypeRecord> queryExpression = new DynamoDBQueryExpression<TaskTypeRecord>()
				.withKeyConditionExpression("userId = :userId")
				.withExpressionAttributeValues(ImmutableMap.of(
						":userId", new AttributeValue().withS(userId)));
		PaginatedQueryList<TaskTypeRecord> tasks = mapper.query(TaskTypeRecord.class, queryExpression);
		return tasks.stream().map(taskTypeRecord -> taskTypeRecord.toTaskType()).collect(Collectors.toList());
	}

	@Override
	public void save(TaskType taskType, String userId) {
		mapper.save(TaskTypeRecord.fromTaskType(taskType, userId));
	}

	@Override
	public void remove(TaskType taskType, String userId) {
		mapper.delete(TaskTypeRecord.fromTaskType(taskType, userId));
	}
}
