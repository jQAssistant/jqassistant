package com.buschmais.jqassistant.neo4j.backend.neo4jv3.extension;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.tika.Tika;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.server.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unmanaged Neo4j extension that serves static content from classpath resources
 * located in {@link #CONTENT_PATH}.
 */
@Path("/")
public class StaticContentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticContentResource.class);

    private static final String CONTENT_PATH = "/META-INF/jqassistant-static-content/";

    private static final Tika TIKA = new Tika();

    public StaticContentResource(@Context Config configuration, @Context WebServer server) {
        LOGGER.debug("Initializing, serving static content from classpath resources located '{}'.", CONTENT_PATH);
    }

    @GET
    @Path("{file:(?i).+}")
    public Response file(@PathParam("file") String file) {
        InputStream fileStream = StaticContentResource.class.getResourceAsStream(CONTENT_PATH + file);
        if (fileStream != null) {
            String mimeType = TIKA.detect(file);
            return mimeType != null ? Response.ok(fileStream, mimeType).build() : Response.ok(fileStream).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

}
