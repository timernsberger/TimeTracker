package com.times6.timeTracker.db;

import com.times6.timeTracker.TaskType;

import java.util.Collection;

public interface TaskTypeDao {
    Collection<TaskType> getAll(String userId);
    void save(TaskType taskType, String userId);
    void remove(TaskType taskType, String userId);
}
