package com.times6.timeTracker.db.sql;

import com.times6.timeTracker.TaskType;
import com.times6.timeTracker.db.TaskTypeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class TaskTypeDaoTest extends DaoTestBase {
    private TaskTypeDao dao;

    @BeforeEach
    public void createDao() {
        dao = new SqlTaskTypeDao(db);
    }

    @Test
    public void saveStoresRecord() {
        String userId = "test user";
        TaskType taskType = TaskType.builder()
                .category("Test")
                .name("test the dao")
                .build();

        dao.save(taskType, userId);

        TaskTypeRecordId key = TaskTypeRecordId.builder()
                .userId(USER_ID)
                .category(taskType.getCategory())
                .taskName(taskType.getName())
                .build();
        TaskTypeRecord result = db.find(TaskTypeRecord.class, key);

        assertThat(result, notNullValue());
        assertThat(result.toTaskType(), equalTo(taskType));
    }

    @Test
    public void getAllReturnsAllRecordsForSpecificUser() {
        TaskTypeRecord userRecord1 = getTaskTypeRecord(USER_ID, "foo", "asdf");
        TaskTypeRecord userRecord2 = getTaskTypeRecord(USER_ID, "foo", "qwer");
        TaskTypeRecord userRecord3 = getTaskTypeRecord(USER_ID, "bar", "wasd");
        TaskTypeRecord otherRecord = getTaskTypeRecord("someone else", "foo", "asdf");

        db.saveAll(Arrays.asList(userRecord1, userRecord2, userRecord3, otherRecord));

        Collection<TaskType> results = dao.getAll(USER_ID);

        assertThat(results, hasSize(3));
        assertThat(results, containsInAnyOrder(userRecord1.toTaskType(), userRecord2.toTaskType(), userRecord3.toTaskType()));
    }

    private TaskTypeRecord getTaskTypeRecord(String userId, String category, String name) {
        TaskTypeRecord record = TaskTypeRecord.builder()
                .id(TaskTypeRecordId.builder()
                    .userId(userId)
                    .category(category)
                    .taskName(name)
                    .build())
                .build();
        return record;
    }
}
