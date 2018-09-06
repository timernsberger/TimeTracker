package com.times6.timeTracker.db.sql;

import com.times6.timeTracker.TaskType;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "task_types")
@Data
@Builder
public class TaskTypeRecord {
    @EmbeddedId
    private TaskTypeRecordId id;

    public TaskType toTaskType() {
        return TaskType.builder()
                .category(id.getCategory())
                .name(id.getTaskName())
                .build();
    }

    public static TaskTypeRecord fromTaskType(TaskType taskType, String userId) {
        return TaskTypeRecord.builder()
                .id(TaskTypeRecordId.builder()
                    .userId(userId)
                    .category(taskType.getCategory())
                    .taskName(taskType.getName())
                    .build())
                .build();
    }
}
