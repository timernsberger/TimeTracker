package com.times6.timeTracker.service;

import com.times6.timeTracker.TaskType;
import com.times6.timeTracker.db.TaskTypeDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;

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

        ArgumentCaptor<TaskType> captor = ArgumentCaptor.forClass(TaskType.class);
        verify(dao).save(captor.capture(), any());
        TaskType record = captor.getValue();
        assertThat(record, equalTo(taskType));
    }

    @Test
    public void getsAllTaskTypesThatBelongToTheUser() {
        TaskType user1 = createRecord("Test", "run tests");
        TaskType user2 = createRecord("test", "run some more tests");

        when(dao.getAll(USER_ID)).thenReturn(Arrays.asList(user1, user2));

        Collection<TaskType> results = controller.getAll();

        assertThat(results, containsInAnyOrder(user1, user2));
        verify(dao).getAll(USER_ID);
    }

    @Test
    public void removeRemovesRecordFromDatabase() {
        TaskType taskType = TaskType.builder()
                .category("test")
                .name("add task types")
                .build();

        controller.remove(taskType);

        ArgumentCaptor<TaskType> captor = ArgumentCaptor.forClass(TaskType.class);
        verify(dao).remove(captor.capture(), any());
        TaskType record = captor.getValue();
        assertThat(record, equalTo(taskType));
    }

    private TaskType createRecord(String category, String name) {
        return TaskType.builder()
                .category(category)
                .name(name)
                .build();
    }
}
