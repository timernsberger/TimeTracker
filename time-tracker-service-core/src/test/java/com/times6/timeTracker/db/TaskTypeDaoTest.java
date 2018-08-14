package com.times6.timeTracker.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.google.common.collect.ImmutableMap;
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
    private TaskTypeDao dao;

    @BeforeEach
    public void createDao() {
        dao = new TaskTypeDao(dynamo);
    }

    @Test
    public void saveStoresRecord() {
        TaskTypeRecord record = new TaskTypeRecord().builder()
            .userId("test user")
            .category("Test")
            .name("test the dao")
            .build();
        record.setRangeKey(record.getCategory() + "|" + record.getName());

        dao.save(record);

        GetItemResult result = dynamo.getItem("task_types", ImmutableMap.<String, AttributeValue>builder()
            .put("userId", new AttributeValue().withS(record.getUserId()))
            .put("taskType", new AttributeValue().withS(record.getRangeKey()))
            .build());
        Map<String, AttributeValue> rawItem = result.getItem();

        assertThat(rawItem, notNullValue());
        assertThat(rawItem.get("name").getS(), equalTo(record.getName()));
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

        Collection<TaskTypeRecord> results = dao.getAll(userId);

        assertThat(results, hasSize(3));
        assertThat(results, containsInAnyOrder(userRecord1, userRecord2, userRecord3));
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
