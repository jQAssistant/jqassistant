package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.buschmais.jqassistant.core.plugin.impl.JQAssistantPropertiesImpl;

/**
 * Service to return the current JQAssistant version.
 */
@Path("/version")
public class VersionService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion() {

        return JQAssistantPropertiesImpl.getInstance().getVersion();
    }
}
