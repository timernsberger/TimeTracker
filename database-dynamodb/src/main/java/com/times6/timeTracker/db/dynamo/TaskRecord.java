package com.times6.timeTracker.db.dynamo;

import java.time.Instant;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.times6.timeTracker.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "tasks")
public class TaskRecord {
	@DynamoDBHashKey
	private String userId;
	@DynamoDBTypeConverted(converter = DynamoDBMapperInstantConverter.class)
	@DynamoDBRangeKey
	private Instant timeStarted;
	private String taskName;
	private String category;
	@DynamoDBTypeConverted(converter = DynamoDBMapperInstantConverter.class)
	private Instant timeEnded;
	
	public Task toTask() {
		return Task.builder().name(taskName).category(category).timeStarted(timeStarted).timeEnded(timeEnded).build();
	}
	
	public static TaskRecord fromTask(Task task, String userId) {
		return TaskRecord.builder()
				.userId(userId)
				.taskName(task.getName())
				.category(task.getCategory())
				.timeStarted(task.getTimeStarted())
				.timeEnded(task.getTimeEnded())
				.build();
	}
}
