package com.times6.timeTracker.db.sql;

import com.times6.timeTracker.TaskType;
import com.times6.timeTracker.db.TaskTypeDao;
import com.times6.timeTracker.db.sql.query.QTaskTypeRecord;
import io.ebean.EbeanServer;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SqlTaskTypeDao implements TaskTypeDao {
    private EbeanServer db;

    @Inject
    public SqlTaskTypeDao(EbeanServer db) {
        this.db = db;
    }

    @Override
    public Collection<TaskType> getAll(String userId) {
        QTaskTypeRecord query = new QTaskTypeRecord(db);
        query.id.userId.eq(userId);
        List<TaskTypeRecord> records = query.findList();
        return records.stream().map(TaskTypeRecord::toTaskType).collect(Collectors.toList());
    }

    @Override
    public void save(TaskType taskType, String userId) {
        TaskTypeRecord record = TaskTypeRecord.fromTaskType(taskType, userId);
        db.save(record);
    }

    @Override
    public void remove(TaskType taskType, String userId) {
        TaskTypeRecord record = TaskTypeRecord.fromTaskType(taskType, userId);
        db.delete(record);
    }
}
