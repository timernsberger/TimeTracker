package com.times6.timeTracker.service;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.times6.timeTracker.db.TaskTypeDao;
import com.times6.timeTracker.db.TaskTypeRecord;

@Path("/task/type")
@Produces(MediaType.APPLICATION_JSON)
public class TaskTypeController {
	
	private TaskTypeDao dao;
	
	@Context 
	private HttpServletRequest request;
	
	@Inject
	public TaskTypeController(TaskTypeDao dao) {
		this.dao = dao;
	}
	
	@GET
	@Path("/")
	public Collection<TaskType> getAll() {
		return dao
				.getAll((String)request.getAttribute("userId"))
				.stream()
				.map(t -> t.toTaskType())
				.collect(Collectors.toList());
	}
	
	@PUT
	@Path("/")
	public Response add(TaskType taskType) {
		TaskTypeRecord record = recordFromRequestParam(taskType);
		dao.save(record);
		return Response.status(Status.CREATED).build();
	}

	@DELETE
	@Path("/")
	public Response remove(TaskType taskType) {
		TaskTypeRecord record = recordFromRequestParam(taskType);
		dao.remove(record);
		return Response.status(Status.OK).build();
	}

	private TaskTypeRecord recordFromRequestParam(TaskType taskType) {
		return TaskTypeRecord.fromTaskType(taskType, (String)request.getAttribute("userId"));
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
}
