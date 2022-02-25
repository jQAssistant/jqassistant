package com.buschmais.jqassistant.neo4j.backend.neo4jv3.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.tika.Tika;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.server.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static lombok.AccessLevel.PRIVATE;

/**
 * Unmanaged Neo4j extension that serves static content from classpath resources
 * located in {@link #CONTENT_PATH}.
 */
@Path("/")
public class StaticContentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticContentResource.class);

    private static final String CONTENT_PATH = "META-INF/jqassistant-static-content/";
    private static final String INDEX_HTML = "index.html";

    private static final Tika TIKA = new Tika();

    private static final Cache<String, Optional<StaticResource>> RESOURCE_CACHE = Caffeine.newBuilder().maximumSize(256).build();

    private ClassLoader classLoader;

    public StaticContentResource(@Context Config configuration, @Context WebServer server, @Context ClassLoader classLoader) {
        LOGGER.debug("Initializing, serving static content from classpath resources located '{}'.", CONTENT_PATH);
        this.classLoader = classLoader;
    }

    @GET
    @Path("{file:(?i).+}")
    public Response file(@PathParam("file") String file) throws IOException {
        Optional<StaticResource> cacheResult = RESOURCE_CACHE.get(file, f -> resolveFileOrDirectoryResource(f));
        if (cacheResult.isPresent()) {
            StaticResource staticResource = cacheResult.get();
            URL resource = staticResource.getResource();
            String mimeType = staticResource.getMimeType();
            InputStream stream = resource.openStream();
            return mimeType != null ? Response.ok(stream, mimeType).build() : Response.ok(stream).build();
        }
        // Use the TCCL which also covers the plugin classpath
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Resolves the given path to a {@link StaticResource}.
     *
     * @param path
     *     The path.
     * @return An {@link Optional} of a {@link StaticResource}.
     */
    private Optional<StaticResource> resolveFileOrDirectoryResource(String path) {
        if (path.endsWith("/")) {
            // Requested path represents a folder, try to resolve index document.
            return resolve(path + INDEX_HTML);
        } else {
            // Verify if requested path represents a folder and contains an index document.
            Optional<StaticResource> resource = resolve(path + "/" + INDEX_HTML);
            if (resource.isPresent()) {
                return resource;
            }
        }
        return resolve(path);
    }

    private Optional<StaticResource> resolve(String resource) {
        LOGGER.debug("Resolving resource {} using context class loader {}.", resource, classLoader);
        URL url = classLoader.getResource(CONTENT_PATH + resource);
        if (url == null) {
            LOGGER.debug("No classpath resource found for '{}'.", resource);
            return Optional.empty();
        }
        try {
            String mimeType = TIKA.detect(url);
            LOGGER.debug("Resource {} with mime type {} found for path {}.", url, mimeType, resource);
            return Optional.of(StaticResource.builder().resource(url).mimeType(mimeType).build());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot determine mime type for " + url, e);
        }
    }

    /**
     * Describes a static resource.
     */
    @Builder
    @Getter
    @AllArgsConstructor(access = PRIVATE)
    @ToString
    private static final class StaticResource {

        private final URL resource;

        private final String mimeType;

    }
}
