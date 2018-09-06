package com.times6.timeTracker.service;
import java.time.Instant;
import java.util.List;

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

import com.times6.timeTracker.Task;
import com.times6.timeTracker.db.TaskDao;

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
		Task task = taskDao.getLatest(getUserId());
		return task;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/current")
	public boolean addTask(Task task) {
		String userId = getUserId();
		Task latestTask = taskDao.getLatest(userId);
		if(latestTask != null && latestTask.getTimeEnded() == null) {
			taskDao.completeTask(userId, latestTask.getTimeStarted(), Instant.now());
		}
		if(task.getTimeStarted() == null) {
			task.setTimeStarted(Instant.now());
		}
		taskDao.save(task, userId);
		
		return true;
	}
	
	@GET
	@Path("/history")
	public Response getTaskHistory(@QueryParam(value="startTime") Long startTime, @QueryParam(value="endTime") Long endTime) {
		String userId = getUserId();
		if(startTime == null) {
			return Response.status(Status.BAD_REQUEST).entity("requires a numeric value for startTime").build();
		}
		Instant endInstant;
		if(endTime == null) {
			endInstant = Instant.now();
		} else {
			endInstant = Instant.ofEpochSecond(endTime);
		}
		List<Task> tasks = taskDao.getRange(userId, Instant.ofEpochSecond(startTime), endInstant);
		return Response.ok(tasks).build();
	}

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    private String getUserId() {
		return (String)request.getAttribute("userId");
	}
}
