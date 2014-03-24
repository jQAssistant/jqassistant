package com.buschmais.jqassistant.plugin.jaxrs.test.set.beans;

import javax.ws.rs.*;
import javax.xml.ws.Response;

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

	@HEAD
	@Path("/testHead")
	void testHead();
	
	@OPTIONS
	@Path("/testOptions")
	Response<String> testOptions();
}
