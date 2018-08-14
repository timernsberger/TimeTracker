package com.times6.timeTracker.service;

import com.amazonaws.AmazonClientException;
import com.times6.timeTracker.db.TaskDao;
import com.times6.timeTracker.db.TaskRecord;
import com.times6.timeTracker.db.TaskTypeDao;
import com.times6.timeTracker.db.TaskTypeRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TaskTypeControllerTest {
    private static final String USER_ID = "test user";

    @Mock
    private HttpServletRequest request;

    @Mock
    private TaskTypeDao dao;

    private TaskTypeController controller;

    @BeforeEach
    public void createController() {
        controller = new TaskTypeController(dao);
        controller.setRequest(request);
    }

    @BeforeEach
    public void registerUserIdHeader() {
        when(request.getAttribute("userId")).thenReturn(USER_ID);
    }

    @AfterEach
    public void verifyUserIdHeaderWasUsed() {
        verify(request).getAttribute("userId");
    }

    @Test
    public void addSavesTaskTypeToDatabase() {
        TaskType taskType = TaskType.builder()
                .category("test")
                .name("add task types")
                .build();

        controller.add(taskType);

        ArgumentCaptor<TaskTypeRecord> captor = ArgumentCaptor.forClass(TaskTypeRecord.class);
        verify(dao).save(captor.capture());
        TaskTypeRecord record = captor.getValue();
        assertThat(record.toTaskType(), equalTo(taskType));
        assertThat(record.getRangeKey(), containsString(taskType.getCategory()));
        assertThat(record.getRangeKey(), containsString(taskType.getName()));
    }

    @Test
    public void getsAllTaskTypesThatBelongToTheUser() {
        TaskTypeRecord user1 = createRecord("Test", "run tests");
        TaskTypeRecord user2 = createRecord("test", "run some more tests");

        when(dao.getAll(USER_ID)).thenReturn(Arrays.asList(user1, user2));

        Collection<TaskType> results = controller.getAll();

        assertThat(results, containsInAnyOrder(user1.toTaskType(), user2.toTaskType()));
        verify(dao).getAll(USER_ID);
    }

    @Test
    public void removeRemovesRecordFromDatabase() {
        TaskType taskType = TaskType.builder()
                .category("test")
                .name("add task types")
                .build();

        controller.remove(taskType);

        ArgumentCaptor<TaskTypeRecord> captor = ArgumentCaptor.forClass(TaskTypeRecord.class);
        verify(dao).remove(captor.capture());
        TaskTypeRecord record = captor.getValue();
        assertThat(record.toTaskType(), equalTo(taskType));
        assertThat(record.getRangeKey(), containsString(taskType.getCategory()));
        assertThat(record.getRangeKey(), containsString(taskType.getName()));
    }

    private TaskTypeRecord createRecord(String category, String name) {
        TaskTypeRecord record = TaskTypeRecord.builder()
                .category(category)
                .name(name)
                .build();
        record.setRangeKey(record.getCategory() + "|" + record.getName());
        return record;
    }
}
