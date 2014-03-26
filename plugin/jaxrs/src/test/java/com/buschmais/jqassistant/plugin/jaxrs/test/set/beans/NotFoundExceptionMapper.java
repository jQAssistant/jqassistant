package com.buschmais.jqassistant.plugin.jaxrs.test.set.beans;

import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * A simple exception mapper for {@link NotFoundException}
 * 
 * @author Aparna Chaudhary
 */
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	@Override
	public Response toResponse(NotFoundException exception) {
		return Response.status(NOT_FOUND).entity(exception.getMessage()).build();
	}

}
