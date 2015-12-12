package com.buschmais.jqassistant.plugin.xml.api.scanner;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

public class XMLFileFilterTest {

    @Test
    public void matchIfRootElementISRequestedOne() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<project><a></<a></project>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project"), is(true));
    }

    @Test
    public void matchIfRootElementISRequestedOneAndDocumentHasDeclaration() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><a></<a></project>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project"), is(true));
    }

    @Test
    public void doesNotMatchIfRootElementISNotRequestedOne() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<p><a></<a></p>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project"), is(false));
    }

    @Test
    public void doesNotMatchIfDocumentIsEmpty() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project"), is(false));
    }

    @Test
    public void doesNotMatchIfDocumentIsInvalid() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<p><a></<a></z>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "project"), is(false));
    }

    @Test
    public void doesMatchIfRootElementAndNamespaceAreRequestedOnes() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<p xmlns:h=\"http://www\"><a></<a></z>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www"), is(true));
    }

    @Test
    public void doesMatchIfRootElementAndNamespaceAreRequestedOnesMultipleNameSpaces() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<p xmlns:h=\"http://www\" xmlns:b=\"http://zzz\"><a></<a></z>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www"), is(true));
    }

    @Test
    public void doesNotMatchIfRootElementIsWrongAndNamespaceAreRequestedIfCorrect() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<e xmlns:h=\"http://www\"><a></<a></e>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www"), is(false));
    }

    @Test
    public void doesNotMatchIfRootElementIsWrongAndNamespaceAreRequestedIsWrong() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream("<p xmlns:h=\"http://www\"><a></<a></z>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "e", "http://yyy"), is(false));
    }

    @Test
    public void doesNotMatchIfRootElementIsWrongAndNamespaceIsRequestedONe() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<p xmlns:h=\"http://yyyy\"><a></<a></p>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://yyyy"), is(true));
        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "e", "http://yyyy"), is(false));
    }


    @Test
    public void doesNotMatchIfDocumentIsEmptyForMatchingByRootElementAndNamespace() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("   ".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www"), is(false));
    }

    @Test
    public void doesNotMatchIfDocumentIsInvalidForMatchingByRootElementAndNamespace() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream("<p xmlns:h=\"http://www\"><a></<a>".getBytes());
        FileResource resource = Mockito.mock(FileResource.class);

        doReturn(stream).when(resource).createStream();

        assertThat(XMLFileFilter.rootElementMatches(resource, "/path/", "p", "http://www"), is(true));
    }
}