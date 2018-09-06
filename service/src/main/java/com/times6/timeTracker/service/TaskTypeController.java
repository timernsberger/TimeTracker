package com.times6.timeTracker.service;

import java.util.Collection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.times6.timeTracker.TaskType;
import com.times6.timeTracker.db.TaskTypeDao;

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
		return dao.getAll(getUserId());
	}
	
	@PUT
	@Path("/")
	public Response add(TaskType taskType) {
		dao.save(taskType, getUserId());
		return Response.status(Status.CREATED).build();
	}

	@DELETE
	@Path("/")
	public Response remove(TaskType taskType) {
		dao.remove(taskType, getUserId());
		return Response.status(Status.OK).build();
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	private String getUserId() {
		return (String)request.getAttribute("userId");
	}
}
