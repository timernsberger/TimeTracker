package com.times6.timeTracker.db.sql;


import lombok.Builder;
import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Builder
@Embeddable
public class TaskTypeRecordId {
    private String userId;
    private String taskName;
    private String category;
}
