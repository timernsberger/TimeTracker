package com.times6.timeTracker.db.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.collect.ImmutableMap;
import com.times6.timeTracker.Task;
import com.times6.timeTracker.db.TaskDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DynamoTaskDao implements TaskDao {
    private DynamoDBMapper mapper;

    @Inject
    public DynamoTaskDao(AmazonDynamoDB dynamo) {
        mapper = new DynamoDBMapper(dynamo);
    }

    @Override
    public Task getLatest(String userId) {
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
            return tasks.iterator().next().toTask();
        }
    }

    @Override
    public List<Task> getRange(String userId, Instant startTime, Instant endTime) {
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
        return records.stream().map(taskRecord -> taskRecord.toTask()).collect(Collectors.toList());
    }

    @Override
    public void save(Task task, String userId) {
        mapper.save(TaskRecord.fromTask(task, userId));
    }

}
