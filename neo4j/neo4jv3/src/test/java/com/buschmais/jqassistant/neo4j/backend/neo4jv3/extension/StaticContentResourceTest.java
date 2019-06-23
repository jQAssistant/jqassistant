package com.buschmais.jqassistant.neo4j.backend.neo4jv3.extension;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.server.web.WebServer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ExtendWith(MockitoExtension.class)
public class StaticContentResourceTest {

    @Mock
    private Config configuration;

    @Mock
    private WebServer server;

    private StaticContentResource staticContentResource;

    @BeforeEach
    public void setUp() {
        staticContentResource = new StaticContentResource(configuration, server);
    }

    @Test
    public void htmlResource() throws IOException {
        Response response = staticContentResource.file("/test/index.html");

        verifyIndexResponse(response);
    }

    @Test
    public void indexFromFolderWithTrailingSlash() throws IOException {
        Response response = staticContentResource.file("/test/");

        verifyIndexResponse(response);
    }

    private void verifyIndexResponse(Response response) throws IOException {
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        MultivaluedMap<String, Object> metadata = response.getMetadata();
        MediaType mediaType = (MediaType) metadata.getFirst("Content-Type");
        assertThat(mediaType, is(MediaType.TEXT_HTML_TYPE));
        Object entity = response.getEntity();
        assertThat(entity, instanceOf(InputStream.class));
        String content = IOUtils.toString((InputStream) entity, "UTF-8");
        assertThat(content, equalTo("<h1>Test</h1>\n"));
    }

    @Test
    public void nonExistingResource() throws IOException {
        Response response = staticContentResource.file("/nonExistingResource.png");

        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }
}
