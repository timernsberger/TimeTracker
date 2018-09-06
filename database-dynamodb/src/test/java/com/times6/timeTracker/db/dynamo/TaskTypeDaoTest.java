package com.times6.timeTracker.db.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.google.common.collect.ImmutableMap;
import com.times6.timeTracker.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;

public class TaskTypeDaoTest extends DaoTestBase {
    private DynamoTaskTypeDao dao;

    @BeforeEach
    public void createDao() {
        dao = new DynamoTaskTypeDao(dynamo);
    }

    @Test
    public void saveStoresRecord() {
        String userId = "test user";
        TaskType taskType = new TaskType().builder()
            .category("Test")
            .name("test the dao")
            .build();

        dao.save(taskType, userId);

        GetItemResult result = dynamo.getItem("task_types", ImmutableMap.<String, AttributeValue>builder()
            .put("userId", new AttributeValue().withS(userId))
            .put("taskType", new AttributeValue().withS(taskType.getCategory() + "|" + taskType.getName()))
            .build());
        Map<String, AttributeValue> rawItem = result.getItem();

        assertThat(rawItem, notNullValue());
        assertThat(rawItem.get("name").getS(), equalTo(taskType.getName()));
    }

    @Test
    public void getAllReturnsAllRecordsForSpecificUser() {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

        String userId = "user";

        TaskTypeRecord userRecord1 = getTaskTypeRecord(userId, "foo", "asdf");
        TaskTypeRecord userRecord2 = getTaskTypeRecord(userId, "foo", "qwer");
        TaskTypeRecord userRecord3 = getTaskTypeRecord(userId, "bar", "wasd");
        TaskTypeRecord otherRecord = getTaskTypeRecord("someone else", "foo", "asdf");

        mapper.batchSave(userRecord1, userRecord2, userRecord3, otherRecord);

        Collection<TaskType> results = dao.getAll(userId);

        assertThat(results, hasSize(3));
        assertThat(results, containsInAnyOrder(userRecord1.toTaskType(), userRecord2.toTaskType(), userRecord3.toTaskType()));
    }

    private TaskTypeRecord getTaskTypeRecord(String userId, String category, String name) {
        TaskTypeRecord record = TaskTypeRecord.builder()
                .userId(userId)
                .category(category)
                .name(name)
                .build();
        record.setRangeKey(record.getCategory() + "|" + record.getName());
        return record;
    }
}
