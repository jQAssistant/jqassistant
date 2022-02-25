package com.buschmais.jqassistant.neo4j.backend.neo4jv3.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.server.web.WebServer;

import static java.lang.System.lineSeparator;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class StaticContentResourceTest {

    private static final MediaType TEXT_CSS_TYPE = new MediaType("text", "css");

    @Mock
    private Config configuration;

    @Mock
    private WebServer server;

    private StaticContentResource staticContentResource;

    private TracingClassLoader classLoader = new TracingClassLoader();

    @BeforeEach
    void setUp() {
        staticContentResource = new StaticContentResource(configuration, server, classLoader);
    }

    @Test
    void htmlResource() throws IOException {
        Response response = staticContentResource.file("/test/other.html");

        verifyIndexResponse(response, "<h1>Other</h1>" + lineSeparator(), TEXT_HTML_TYPE);
        assertThat("ClassLoader must be used to resolve resource.", classLoader.getLastResource().getPath(), endsWith("/test/other.html"));
    }

    @Test
    void cssResource() throws IOException {
        Response response = staticContentResource.file("/test/styles.css");

        verifyIndexResponse(response, "/* Styles */" + lineSeparator(), TEXT_CSS_TYPE);
    }

    @Test
    void indexFromFolderWithTrailingSlash() throws IOException {
        Response response = staticContentResource.file("/test/");

        verifyIndexResponse(response, "<h1>Index</h1>" + lineSeparator(), TEXT_HTML_TYPE);
    }

    @Test
    void indexFromFolderWithoutTrailingSlash() throws IOException {
        Response response = staticContentResource.file("/test");

        verifyIndexResponse(response, "<h1>Index</h1>" + lineSeparator(), TEXT_HTML_TYPE);
    }

    private void verifyIndexResponse(Response response, String expectedContent, MediaType expectedMimeType) throws IOException {
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        MultivaluedMap<String, Object> metadata = response.getMetadata();
        MediaType mediaType = (MediaType) metadata.getFirst("Content-Type");
        assertThat(mediaType, is(expectedMimeType));
        Object entity = response.getEntity();
        assertThat(entity, instanceOf(InputStream.class));
        String content = IOUtils.toString((InputStream) entity, "UTF-8");
        assertThat(content, equalTo(expectedContent));
    }

    @Test
    void nonExistingResource() throws IOException {
        Response response = staticContentResource.file("/nonExistingResource.png");

        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Getter
    private static class TracingClassLoader extends ClassLoader {

        private URL lastResource;

        @Override
        public URL getResource(String name) {
            URL resource = super.getResource(name);
            this.lastResource = resource;
            return resource;
        }
    }
}
