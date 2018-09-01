package com.times6.timeTracker.service;

import com.amazonaws.AmazonClientException;
import com.times6.timeTracker.Task;
import com.times6.timeTracker.db.TaskDao;
import com.times6.timeTracker.db.dynamo.TaskRecord;
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
        TaskRecord record = TaskRecord.fromTask(task, USER_ID);

        when(taskDao.getLatest(USER_ID)).thenReturn(record);

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
            ArgumentCaptor<TaskRecord> recordCaptor = ArgumentCaptor.forClass(TaskRecord.class);
            verify(taskDao).save(recordCaptor.capture());
            TaskRecord storedRecord = recordCaptor.getValue();
            assertThat(storedRecord.getCategory(), equalTo(task.getCategory()));
            assertThat(storedRecord.getTaskName(), equalTo(task.getName()));
            assertThat(storedRecord.getTimeStarted(), equalTo(task.getTimeStarted()));
            assertThat(storedRecord.getUserId(), equalTo(USER_ID));
        }

        @Test
        public void completesCurrentTask() {
            Task newTask = Task.builder()
                    .category("Test")
                    .name("store task")
                    .timeStarted(Instant.now().minus(10L, ChronoUnit.SECONDS))
                    .build();

            TaskRecord existingTask = TaskRecord.builder()
                    .category("Test")
                    .taskName("run test case")
                    .timeStarted(Instant.now().minus(1000L, ChronoUnit.SECONDS))
                    .build();

            when(taskDao.getLatest(USER_ID)).thenReturn(existingTask);

            boolean result = controller.addTask(newTask);

            verify(taskDao).getLatest(USER_ID);

            assertThat(result, equalTo(true));
            ArgumentCaptor<TaskRecord> recordCaptor = ArgumentCaptor.forClass(TaskRecord.class);
            verify(taskDao, times(2)).save(recordCaptor.capture());
            List<TaskRecord> storedRecords = recordCaptor.getAllValues();
            assertThat(storedRecords, hasSize(2));
            TaskRecord updatedExistingTask = storedRecords.get(0);
            assertThat(updatedExistingTask.getUserId(), equalTo(existingTask.getUserId()));
            assertThat(updatedExistingTask.getTimeStarted(), equalTo(existingTask.getTimeStarted()));
            assertThat(updatedExistingTask.getTimeEnded(), notNullValue());
            // TODO: mock end time either with power mocking of Instant.now() or injecting a "now" provide
        }

        @Test
        public void bubblesExceptions() {
            Task task = Task.builder()
                    .category("Test")
                    .name("store task")
                    .timeStarted(Instant.now().minus(10L, ChronoUnit.SECONDS))
                    .build();

            when(taskDao.getLatest(USER_ID)).thenReturn(null);

            doThrow(new AmazonClientException("")).when(taskDao).save(Mockito.any(TaskRecord.class));

            assertThrows(AmazonClientException.class, () -> controller.addTask(task));

            verify(taskDao).save(Mockito.any(TaskRecord.class));
        }
    }

    @Test
    public void requestsTaskHistoryUsingRangeInQueryParams() {
        long start = 100L;
        long end = 100_000L;

        TaskRecord record = TaskRecord.builder()
                .userId(USER_ID)
                .timeStarted(Instant.ofEpochSecond(1000L))
                .timeEnded(Instant.ofEpochSecond(1002L))
                .category("test")
                .taskName("name tasks")
                .build();

        when(taskDao.getRange(eq(USER_ID), Mockito.any(Instant.class), Mockito.any(Instant.class))).thenReturn(Arrays.asList(record));

        Response response = controller.getTaskHistory(start, end);

        assertThat(response.getEntity(), instanceOf(List.class));
        List<Task> responsePayload = (List<Task>)response.getEntity();
        assertThat(responsePayload, hasSize(1));
        assertThat(responsePayload, containsInAnyOrder(record.toTask()));
        verify(taskDao).getRange(USER_ID, Instant.ofEpochSecond(start), Instant.ofEpochSecond(end));
    }
}
