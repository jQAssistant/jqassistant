package com.buschmais.jqassistant.plugin.xml.api.scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

class XMLFileFilterTest {

    @Test
    void matchUnqualifiedRootElement() throws IOException {
        FileResource resource = stubFileResource("<project><a></<a></project>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project")).isEqualTo(true);
    }

    @Test
    void matchUnqualifiedRootElementWithDocumentDeclaration() throws IOException {
        FileResource resource = stubFileResource("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><a></<a></project>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project")).isEqualTo(true);
    }

    @Test
    void rejectUnqualifiedRootElement() throws IOException {
        FileResource resource = stubFileResource("<p><a></<a></p>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project")).isEqualTo(false);
    }

    @Test
    void rejectEmptyDocument() throws IOException {
        FileResource resource = stubFileResource("");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project")).isEqualTo(false);
    }

    @Test
    void rejectInvalidDocument() throws IOException {
        FileResource resource = stubFileResource("<p><a></<a></z>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project")).isEqualTo(false);
    }

    @Test
    void matchFullQualifiedRootElement() throws IOException {
        FileResource resource = stubFileResource("<p xmlns=\"http://www\"><a></<a></p>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www")).isEqualTo(true);
    }

    @Test
    void matchFullQualifiedRootElementWithPrefix() throws IOException {
        FileResource resource = stubFileResource("<h:p xmlns:h=\"http://www\"><a></<a>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www")).isEqualTo(true);
    }

    @Test
    void rejectFullQualifiedRootElement() throws IOException {
        FileResource resource = stubFileResource("<e xmlns=\"http://www\"><a></<a></e>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www")).isEqualTo(false);
    }

    @Test
    void rejectFullQualifiedRootElementWithoutNamespace() throws IOException {
        FileResource resource = stubFileResource("<p <a></<a></p>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www")).isEqualTo(false);
    }

    @Test
    void rejectFullQualifiedRootElementWithDifferentNamespace() throws Exception {
        FileResource resource = stubFileResource("<p xmlns=\"http://www\"><a></<a></z>");

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "e", "http://yyy")).isEqualTo(false);
    }


    private static FileResource stubFileResource(String xml) throws IOException {
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();
        return resource;
    }
}
