package com.times6.timeTracker.db.sql;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;

@Embeddable
@Data
@Builder
public class TaskRecordId implements Serializable {
    private String userId;
    private Instant timeStarted;
}
