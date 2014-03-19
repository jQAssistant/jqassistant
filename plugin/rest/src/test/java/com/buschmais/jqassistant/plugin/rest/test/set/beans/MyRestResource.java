package com.buschmais.jqassistant.plugin.rest.test.set.beans;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * A simple REST resource.
 * 
 * @author Aparna Chaudhary
 */
@Path("/")
public interface MyRestResource {

	@GET
	@Path("/testGet")
	@Produces({ "application/json" })
	String testGet();

	@POST
	@Path("/testPost")
	@Consumes("text/plain")
	@Produces("text/plain")
	String testPost(String message);

	@PUT
	@Path("/testPut")
	@Produces({ "application/json" })
	String testPut();

	@DELETE
	@Path("/testDelete")
	@Produces({ "application/json" })
	String testDelete();
}
