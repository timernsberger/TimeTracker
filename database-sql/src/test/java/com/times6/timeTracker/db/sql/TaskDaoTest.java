package com.times6.timeTracker.db.sql;

import com.times6.timeTracker.Task;
import com.times6.timeTracker.db.TaskDao;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.avaje.datasource.DataSourceConfig;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class TaskDaoTest extends DaoTestBase {
    private TaskDao dao;

    @BeforeEach
    public void createDao() {
        dao = new SqlTaskDao(db);
    }

    @Test
    public void returnsNullIfUserNeverRecordedATask() {
        Task task = dao.getLatest("someone");

        assertThat(task, nullValue());
    }

    @Test
    public void returnsMostRecentWhenUserRecordedMultipleTasks() {
        TaskRecord older = getTaskRecord(USER_ID, "make checklist", Instant.ofEpochSecond(1234), Instant.ofEpochSecond(2345));
        TaskRecord newer = getTaskRecord(USER_ID, "complete checklist", Instant.ofEpochSecond(2345), Instant.ofEpochSecond(1234567890));

        db.saveAll(Arrays.asList(older, newer));

        Task result = dao.getLatest(USER_ID);
        assertThat(result, CoreMatchers.equalTo(newer.toTask()));
    }

    @Test
    public void doesNotReturnRecordBelongingToAnotherUser() {
        TaskRecord userRecord = getTaskRecord(USER_ID, "make checklist", Instant.ofEpochSecond(1234), Instant.ofEpochSecond(2345));
        TaskRecord otherRecord = getTaskRecord("someone else", "complete checklist", Instant.ofEpochSecond(2345), Instant.ofEpochSecond(1234567890));
        db.saveAll(Arrays.asList(userRecord, otherRecord));

        Task result = dao.getLatest(USER_ID);

        assertThat(result, CoreMatchers.equalTo(userRecord.toTask()));
    }

    @Test
    public void saveDoesWhatTheNameSays() {
        Instant startTime = Instant.now();
        Task task = Task.builder()
                .name("test")
                .category("test")
                .timeStarted(startTime)
                .build();
        dao.save(task, USER_ID);

        TaskRecord storedRecord = db.find(TaskRecord.class, TaskRecordId.builder().userId(USER_ID).timeStarted(startTime).build());

        assertThat(db.createQuery(TaskRecord.class).findList().size(), equalTo(1));
        assertThat(storedRecord, notNullValue());
        assertThat(storedRecord.getRecordId().getUserId(), equalTo(USER_ID));
        assertThat(storedRecord.getRecordId().getTimeStarted(), equalTo(startTime));
        assertThat(storedRecord.toTask(), equalTo(task));
    }

    @Test
    public void getRangeOnlyReturnsTasksStartedInTimeRange() {
        TaskRecord userRecord1 = getTaskRecord(USER_ID, "make checklist", Instant.ofEpochSecond(1234), Instant.ofEpochSecond(2345));
        TaskRecord userRecord2 = getTaskRecord(USER_ID, "make checklist", Instant.ofEpochSecond(2345), Instant.ofEpochSecond(2990));
        TaskRecord userRecord3 = getTaskRecord(USER_ID, "make checklist", Instant.ofEpochSecond(2990), Instant.ofEpochSecond(4001));
        TaskRecord userRecord4 = getTaskRecord(USER_ID, "make checklist", Instant.ofEpochSecond(4001), null);
        TaskRecord otherRecord = getTaskRecord("someone else", "complete checklist", Instant.ofEpochSecond(2345), Instant.ofEpochSecond(1234567890));
        db.saveAll(Arrays.asList(userRecord1, userRecord2, userRecord3, userRecord4, otherRecord));

        Collection<Task> results = dao.getRange(USER_ID, Instant.ofEpochSecond(2000), Instant.ofEpochSecond(4000));

        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(userRecord2.toTask(), userRecord3.toTask()));
    }

    private TaskRecord getTaskRecord(String userId, String name, Instant timeStarted, Instant timeEnded) {
        return TaskRecord.builder()
                .recordId(TaskRecordId.builder().userId(userId).timeStarted(timeStarted).build())
                .category("test")
                .taskName(name)
                .timeEnded(timeEnded)
                .build();
    }
}
