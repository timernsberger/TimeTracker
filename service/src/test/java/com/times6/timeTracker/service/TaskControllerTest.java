package com.times6.timeTracker.service;

import com.times6.timeTracker.Task;
import com.times6.timeTracker.db.TaskDao;
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
import javax.ws.rs.core.Response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {
    private static final String USER_ID = "test user";

    @Mock
    private HttpServletRequest request;

    @Mock
    private TaskDao taskDao;

    private TaskController controller;

    @BeforeEach
    public void createController() {
        controller = new TaskController(taskDao);
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
    public void getsCurrentTask() {
        Task task = Task.builder()
                .category("Test")
                .name("get current task")
                .timeStarted(Instant.now())
                .build();

        when(taskDao.getLatest(USER_ID)).thenReturn(task);

        Task result = controller.getCurrentTask();
        assertThat(result, equalTo(task));

        verify(taskDao).getLatest(USER_ID);
    }

    @Test
    public void returnsNullIfDatabaseHasNoLatestTask() {
        when(taskDao.getLatest(USER_ID)).thenReturn(null);

        Task result = controller.getCurrentTask();
        assertThat(result, nullValue());

        verify(taskDao).getLatest(USER_ID);
    }

    @Nested
    public class AddTaskTest {
        @Test
        public void storesTaskInDatabase() {
            Task task = Task.builder()
                    .category("Test")
                    .name("store task")
                    .timeStarted(Instant.now().minus(10L, ChronoUnit.SECONDS))
                    .build();

            when(taskDao.getLatest(USER_ID)).thenReturn(null);

            boolean result = controller.addTask(task);

            verify(taskDao).getLatest(USER_ID);

            assertThat(result, equalTo(true));
            ArgumentCaptor<Task> recordCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskDao).save(recordCaptor.capture(), any());
            Task storedRecord = recordCaptor.getValue();
            assertThat(storedRecord.getCategory(), equalTo(task.getCategory()));
            assertThat(storedRecord.getName(), equalTo(task.getName()));
            assertThat(storedRecord.getTimeStarted(), equalTo(task.getTimeStarted()));
        }

        @Test
        public void completesCurrentTask() {
            Instant now = Instant.now();
            Task newTask = Task.builder()
                    .category("Test")
                    .name("store task")
                    .timeStarted(now.minus(10L, ChronoUnit.SECONDS))
                    .build();

            Task existingTask = Task.builder()
                    .category("Test")
                    .name("run test case")
                    .timeStarted(now.minus(1000L, ChronoUnit.SECONDS))
                    .build();

            when(taskDao.getLatest(USER_ID)).thenReturn(existingTask);

            boolean result = controller.addTask(newTask);

            verify(taskDao).getLatest(USER_ID);

            assertThat(result, equalTo(true));
            ArgumentCaptor<Instant> endTimeCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(taskDao).completeTask(eq(USER_ID), eq(existingTask.getTimeStarted()), endTimeCaptor.capture());
            Instant savedEndTime = endTimeCaptor.getValue();
            // TODO: mock end time either with power mocking of Instant.now() or injecting a "now" provider
            assertThat(savedEndTime, greaterThanOrEqualTo(now));
            verify(taskDao).save(eq(newTask), eq(USER_ID));
        }

        @Test
        public void bubblesExceptions() {
            Task task = Task.builder()
                    .category("Test")
                    .name("store task")
                    .timeStarted(Instant.now().minus(10L, ChronoUnit.SECONDS))
                    .build();

            when(taskDao.getLatest(USER_ID)).thenReturn(null);

            doThrow(new RuntimeException("")).when(taskDao).save(any(), any());

            assertThrows(RuntimeException.class, () -> controller.addTask(task));

            verify(taskDao).save(any(), any());
        }
    }

    @Test
    public void requestsTaskHistoryUsingRangeInQueryParams() {
        long start = 100L;
        long end = 100_000L;

        Task record = Task.builder()
                .timeStarted(Instant.ofEpochSecond(1000L))
                .timeEnded(Instant.ofEpochSecond(1002L))
                .category("test")
                .name("name tasks")
                .build();

        when(taskDao.getRange(eq(USER_ID), Mockito.any(Instant.class), Mockito.any(Instant.class))).thenReturn(Arrays.asList(record));

        Response response = controller.getTaskHistory(start, end);

        assertThat(response.getEntity(), instanceOf(List.class));
        List<Task> responsePayload = (List<Task>)response.getEntity();
        assertThat(responsePayload, hasSize(1));
        assertThat(responsePayload, containsInAnyOrder(record));
        verify(taskDao).getRange(USER_ID, Instant.ofEpochSecond(start), Instant.ofEpochSecond(end));
    }
}
