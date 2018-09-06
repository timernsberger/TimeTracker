package com.times6.timeTracker.db.sql;

import com.google.inject.Inject;
import com.times6.timeTracker.Task;
import com.times6.timeTracker.db.TaskDao;
import com.times6.timeTracker.db.sql.query.QTaskRecord;
import io.ebean.EbeanServer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SqlTaskDao implements TaskDao {

    private EbeanServer db;

    @Inject
    public SqlTaskDao(EbeanServer db) {
        this.db = db;
    }

    @Override
    public Task getLatest(String userId) {
        QTaskRecord query = new QTaskRecord(db);
        query.recordId.userId.eq(userId);
        query.orderBy().recordId.timeStarted.desc();
        query.setMaxRows(1);
        Optional<TaskRecord> record = query.findOneOrEmpty();
        return record.map(TaskRecord::toTask).orElse(null);
    }

    @Override
    public List<Task> getRange(String userId, Instant startTime, Instant endTime) {
        QTaskRecord query = new QTaskRecord(db);
        query.recordId.userId.eq(userId);
        query.orderBy().recordId.timeStarted.desc();
        query.recordId.timeStarted.between(startTime, endTime);
        return query.findList().stream().map(TaskRecord::toTask).collect(Collectors.toList());
    }

    @Override
    public void completeTask(String userId, Instant startTime, Instant endTime) {
        TaskRecord record = TaskRecord.builder()
                .recordId(TaskRecordId.builder().userId(userId).timeStarted(startTime).build())
                .timeEnded(endTime)
                .build();
        db.createUpdate(TaskRecord.class, "UPDATE tasks SET time_ended = :time_ended WHERE (user_id = :user_id and time_started = :time_started").execute();
        //db.update(record);
    }

    @Override
    public void save(Task task, String userId) {
        db.save(TaskRecord.fromTask(task, userId));
    }
}
