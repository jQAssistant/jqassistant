package com.buschmais.jqassistant.plugin.jaxrs.test.set.beans;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;

/**
 * A simple context provider implementation.
 * 
 * @author Aparna Chaudhary
 */
@Produces(MediaType.APPLICATION_XML)
public class BookResolver implements ContextResolver<JAXBContext> {

    private JAXBContext jaxbContext;

    public BookResolver() {
        // initialize the context
        this.jaxbContext = null;
    }

    @Override
    public JAXBContext getContext(Class<?> type) {
        if (type.equals(Book.class)) {
            return jaxbContext;
        } else {
            return null;
        }
    }

}
