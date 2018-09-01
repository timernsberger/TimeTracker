package com.times6.timeTracker.db.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.times6.timeTracker.TaskType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "task_types")
public class TaskTypeRecord {
	@DynamoDBHashKey(attributeName = "userId")
	private String userId;
	private String category;
	private String name;
	@DynamoDBRangeKey(attributeName = "taskType")
	private String rangeKey;
	
	public TaskType toTaskType() {
		return TaskType.builder().name(name).category(category).build();
	}
	
	public static TaskTypeRecord fromTaskType(TaskType taskType, String userId) {
		return builder()
				.userId(userId)
				.name(taskType.getName())
				.category(taskType.getCategory())
				.rangeKey(taskType.getCategory() + "|" + taskType.getName())
				.build();
	}
}
