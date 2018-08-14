package com.times6.timeTracker.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class TaskDaoTest extends DaoTestBase {
    private TaskDao dao;

    @BeforeEach
    public void createDao() {
        dao = new TaskDao(dynamo);
    }

    @Nested
    public class GetLatestTest {

        @Test
        public void returnsNullIfUserNeverRecordedATask() {
            TaskRecord record = dao.getLatest("someone");

            assertThat(record, nullValue());
        }

        @Test
        public void returnsMostRecentWhenUserRecordedMultipleTasks() {
            DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

            String userId = "user";

            TaskRecord older = getTaskRecord(userId, "make checklist", 1234, 2345);
            TaskRecord newer = getTaskRecord(userId, "complete checklist", 2345, 1234567890);
            mapper.batchSave(older, newer);

            TaskRecord result = dao.getLatest(userId);

            assertThat(result, equalTo(newer));
        }

        @Test
        public void doesNotReturnRecordBelongingToAnotherUser() {
            DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

            String userId = "user";

            TaskRecord userRecord = getTaskRecord(userId, "make checklist", 1234, 2345);
            TaskRecord otherRecord = getTaskRecord("someone else", "complete checklist", 2345, 1234567890);
            mapper.batchSave(userRecord, otherRecord);

            TaskRecord result = dao.getLatest(userId);

            assertThat(result, equalTo(userRecord));
        }

        @Test
        public void returnsLatestEvenIfTaskIsStillInProgress() {
            DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

            String userId = "user";

            TaskRecord older = getTaskRecord(userId, "make checklist", 1234, 2345);
            TaskRecord newer = getTaskRecord(userId, "complete checklist", 2345, null);
            mapper.batchSave(older, newer);

            TaskRecord result = dao.getLatest(userId);

            assertThat(result, equalTo(newer));
        }
    }

    @Test
    public void saveDoesWhatTheNameSays() {
        TaskRecord record = getTaskRecord("abc", "record task", Instant.now().getEpochSecond(), null);

        dao.save(record);

        GetItemResult getItemResult = dynamo.getItem("tasks", ImmutableMap.<String, AttributeValue>builder()
                .put("userId", new AttributeValue().withS(record.getUserId()))
                .put("timeStarted", new AttributeValue().withN(String.valueOf(record.getTimeStarted().getEpochSecond())))
                .build());

        Map<String, AttributeValue> rawRecord = getItemResult.getItem();

        assertThat(rawRecord, notNullValue());
        assertThat(rawRecord.get("taskName").getS(), equalTo(record.getTaskName()));
        assertThat(rawRecord.get("timeEnded"), nullValue());
    }

    @Test
    public void getRangeOnlyReturnsTasksStartedInTimeRange() {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

        String userId = "user";

        TaskRecord userRecord1 = getTaskRecord(userId, "make checklist", 1234, 2345);
        TaskRecord userRecord2 = getTaskRecord(userId, "make checklist", 2345, 2990);
        TaskRecord userRecord3 = getTaskRecord(userId, "make checklist", 2990, 4001);
        TaskRecord userRecord4 = getTaskRecord(userId, "make checklist", 4001, null);
        TaskRecord otherRecord = getTaskRecord("someone else", "complete checklist", 2345, 1234567890);
        mapper.batchSave(userRecord1, userRecord2, userRecord3, userRecord4, otherRecord);

        Collection<TaskRecord> results = dao.getRange(userId, Instant.ofEpochSecond(2000), Instant.ofEpochSecond(4000));

        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(userRecord2, userRecord3));
    }

    private TaskRecord getTaskRecord(String userId, String name, long startSecond, Integer endSecond) {
        TaskRecord record = TaskRecord.builder()
                .category("Test")
                .taskName(name)
                .timeStarted(Instant.ofEpochSecond(startSecond))
                .userId(userId)
                .build();
        if(endSecond != null) {
            record.setTimeEnded(Instant.ofEpochSecond(endSecond));
        }
        return record;
    }
}
