package com.buschmais.jqassistant.plugin.jaxrs.test.set.beans;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    @Produces({"application/json"})
    String testGet();

    @GET
    @Path("/voidGetMethod")
    void voidGetMethod();

    @POST
    @Path("/testPost")
    @Consumes("text/plain")
    @Produces("text/plain")
    String testPost(String message);

    @PUT
    @Path("/testPut")
    @Produces({"application/json"})
    String testPut();

    @DELETE
    @Path("/testDelete")
    @Produces({"application/json"})
    String testDelete();

    @HEAD
    @Path("/testHead")
    void testHead();

    @OPTIONS
    @Path("/testOptions")
    Response<String> testOptions();

    @Path("/subResource/{id}")
    MySubResource getMySubResource(@PathParam("id") String id);

    @Path("/invalidSubResource")
    MySubResource getMyInvalidSubResource(String id);
}
