package com.times6.timeTracker.db;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.collect.ImmutableMap;
import com.times6.timeTracker.service.Task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskDao {

	private DynamoDBMapper mapper;
	
	@Inject
	public TaskDao(AmazonDynamoDB dynamo) {
		mapper = new DynamoDBMapper(dynamo);
	}
	
	public TaskRecord getLatest(String userId) {
		DynamoDBQueryExpression<TaskRecord> queryExpression = new DynamoDBQueryExpression<TaskRecord>()
				.withKeyConditionExpression("userId = :userId and timeStarted < :timeStarted")
				.withScanIndexForward(false)
				.withExpressionAttributeValues(ImmutableMap.of(
						":userId", new AttributeValue().withS(userId), 
						":timeStarted", new AttributeValue().withN(String.valueOf(Long.MAX_VALUE))))
		;
		PaginatedQueryList<TaskRecord> tasks = mapper.query(TaskRecord.class, queryExpression);
		if(tasks.isEmpty()) {
			return null;
		} else {
			return tasks.iterator().next();
		}
	}
	
	public List<TaskRecord> getRange(String userId, Instant startTime, Instant endTime) {
		DynamoDBMapperInstantConverter instantConverter = new DynamoDBMapperInstantConverter();
		DynamoDBQueryExpression<TaskRecord> queryExpression = new DynamoDBQueryExpression<TaskRecord>()
				.withKeyConditionExpression("userId = :userId and timeStarted between :begin and :end")
				.withExpressionAttributeValues(ImmutableMap.of(
						":userId", new AttributeValue().withS(userId), 
						":begin", new AttributeValue().withN(String.valueOf(instantConverter.convert(startTime))), 
						":end", new AttributeValue().withN(String.valueOf(instantConverter.convert(endTime)))))
		;
		log.info("querying between {} and {} for user {}", instantConverter.convert(startTime), instantConverter.convert(endTime), userId);
		PaginatedQueryList<TaskRecord> tasks = mapper.query(TaskRecord.class, queryExpression);
		List<TaskRecord> records = new ArrayList<>(tasks);
		log.info("got {} tasks", tasks.size());
		return records;
	}
	
	public void save(TaskRecord record) {
		mapper.save(record);
	}
	
	public void addTask(Task task, String userId) {
		TaskRecord record = TaskRecord.fromTask(task, userId);
		mapper.save(record);
	}
}
