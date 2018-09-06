package com.times6.timeTracker.db;

import com.times6.timeTracker.Task;

import java.time.Instant;
import java.util.List;

public interface TaskDao {
    Task getLatest(String userId);
    List<Task> getRange(String userId, Instant startTime, Instant endTime);
    void completeTask(String userId, Instant startTime, Instant endTime);
    void save(Task task, String userId);
}
