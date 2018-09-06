package com.times6.timeTracker.db.sql;

import com.times6.timeTracker.Task;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tasks")
@Data
@Builder
public class TaskRecord {
    @EmbeddedId
    private TaskRecordId recordId;
    private String taskName;
    private String category;
    private Instant timeEnded;

    public Task toTask() {
        return Task.builder()
                .category(category)
                .name(taskName)
                .timeStarted(recordId.getTimeStarted())
                .timeEnded(timeEnded)
                .build();
    }

    public static TaskRecord fromTask(Task task, String userId) {
        return TaskRecord.builder()
                .recordId(TaskRecordId.builder().userId(userId).timeStarted(task.getTimeStarted()).build())
                .category(task.getCategory())
                .taskName(task.getName())
                .timeEnded(task.getTimeEnded())
                .build();
    }
}
