package com.times6.timeTracker.service;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.times6.timeTracker.db.TaskDao;
import com.times6.timeTracker.db.TaskRecord;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
public class TaskController {
	
	private TaskDao taskDao;
	@Context 
	private HttpServletRequest request;

	@Inject
	public TaskController(TaskDao taskDao) {
		this.taskDao = taskDao;
	}
	
	@GET
	@Path("/current")
	public Task getCurrentTask() {
		TaskRecord taskRecord = taskDao.getLatest((String)request.getAttribute("userId"));
		if(taskRecord == null) {
			return null;
		}
		return taskRecord.toTask();
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/current")
	public boolean addTask(Task task) {
		String userId = (String)request.getAttribute("userId");
		TaskRecord lastRecord = taskDao.getLatest(userId);
		if(lastRecord != null && lastRecord.getTimeEnded() == null) {
			lastRecord.setTimeEnded(Instant.now());
			taskDao.save(lastRecord);
		}
		if(task.getTimeStarted() == null) {
			task.setTimeStarted(Instant.now());
		}
		taskDao.save(TaskRecord.fromTask(task, userId));
		
		return true;
	}
	
	@GET
	@Path("/history")
	public Response getTaskHistory(@QueryParam(value="startTime") Long startTime, @QueryParam(value="endTime") Long endTime) {
		String userId = (String)request.getAttribute("userId");
		if(startTime == null) {
			return Response.status(Status.BAD_REQUEST).entity("requires a numeric value for startTime").build();
		}
		Instant endInstant;
		if(endTime == null) {
			endInstant = Instant.now();
		} else {
			endInstant = Instant.ofEpochSecond(endTime);
		}
		List<TaskRecord> records = taskDao.getRange(userId, Instant.ofEpochSecond(startTime), endInstant);
		List<Task> tasks = records.stream().map((record) -> record.toTask()).collect(Collectors.toList());
		return Response.ok(tasks).build();
	}

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}
