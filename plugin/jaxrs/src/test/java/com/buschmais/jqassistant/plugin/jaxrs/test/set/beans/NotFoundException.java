package com.buschmais.jqassistant.plugin.jaxrs.test.set.beans;

import javax.ws.rs.ext.ExceptionMapper;

/**
 * A simple exception to verify {@link ExceptionMapper}
 * 
 * @author Aparna Chaudhary
 */
public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4426747053663869059L;

	public NotFoundException(String message) {
		super(message);
	}

}
