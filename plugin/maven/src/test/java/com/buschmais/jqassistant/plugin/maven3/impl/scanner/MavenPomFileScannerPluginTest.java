package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.PomModelBuilder;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MavenPomFileScannerPluginTest {
    private static final Answer NOT_MOCKED_ANSWER = new MethodNotMockedAnswer();

    private static final Scope DUMMY_SCOPE = new DummyScope();

    @Test
    void acceptAcceptsFilesWithExtensionDotPom() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();

        InputStream inputStream = new ByteArrayInputStream("<bla><a></a></bla>".getBytes(StandardCharsets.UTF_8));

        FileResource fileResource = Mockito.mock(FileResource.class, NOT_MOCKED_ANSWER);

        doReturn(inputStream).when(fileResource).createStream();

        String path = "/a/b/c/pom.xml";

        boolean result = plugin.accepts(fileResource, path, DUMMY_SCOPE);

        assertThat(result).isEqualTo(true);
    }

    @Test
    void acceptAcceptsFilesWithNamePomDotXML() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();

        InputStream inputStream = new ByteArrayInputStream("<bla><a></a></bla>".getBytes(StandardCharsets.UTF_8));

        FileResource fileResource = Mockito.mock(FileResource.class, NOT_MOCKED_ANSWER);

        doReturn(inputStream).when(fileResource).createStream();

        String path = "/a/b/c/d.pom";

        boolean result = plugin.accepts(fileResource, path, DUMMY_SCOPE);

        assertThat(result).isEqualTo(true);
    }

    @Test
    void acceptRefusesFileWithNonXMLExtension() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();

        InputStream inputStream = new ByteArrayInputStream("<project><a></a></project>".getBytes(StandardCharsets.UTF_8));

        FileResource fileResource = Mockito.mock(FileResource.class, NOT_MOCKED_ANSWER);

        doReturn(inputStream).when(fileResource).createStream();

        String path = "/a/b/c/pom.foobar";

        boolean result = plugin.accepts(fileResource, path, DUMMY_SCOPE);

        assertThat(result).isEqualTo(false);
    }

    @Test
    void acceptChecksAcceptsWithRootTagProjectIfExtensionIsXMLAndNonStandardName() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();

        InputStream inputStream = new ByteArrayInputStream("<project><a></a></project>".getBytes(StandardCharsets.UTF_8));

        FileResource fileResource = Mockito.mock(FileResource.class, NOT_MOCKED_ANSWER);

        doReturn(inputStream).when(fileResource).createStream();

        String path = "/a/b/c/d.xml";

        boolean result = plugin.accepts(fileResource, path, DUMMY_SCOPE);

        assertThat(result).isEqualTo(false);
    }

    @Test
    void acceptAcceptsXmlWithMaven400Namespace() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();

        InputStream inputStream = new ByteArrayInputStream(
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"><a></a></project>".getBytes(StandardCharsets.UTF_8));

        FileResource fileResource = Mockito.mock(FileResource.class, NOT_MOCKED_ANSWER);

        doReturn(inputStream).when(fileResource).createStream();

        String path = "/a/b/c/d.xml";

        boolean result = plugin.accepts(fileResource, path, DUMMY_SCOPE);

        assertThat(result).isEqualTo(true);
    }

    @Test
    void acceptAcceptsXmlWithMaven410Namespace() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();

        InputStream inputStream = new ByteArrayInputStream(
                "<project xmlns=\"http://maven.apache.org/POM/4.1.0\"><a></a></project>".getBytes(StandardCharsets.UTF_8));

        FileResource fileResource = Mockito.mock(FileResource.class, NOT_MOCKED_ANSWER);

        doReturn(inputStream).when(fileResource).createStream();

        String path = "/a/b/c/d.xml";

        boolean result = plugin.accepts(fileResource, path, DUMMY_SCOPE);

        assertThat(result).isEqualTo(true);
    }

    @Test
    void acceptChecksRefusesWithDifferentRootTagfExtensionIsXMLAndNonStandardName() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();

        InputStream inputStream = new ByteArrayInputStream("<root><a></a></root>".getBytes(StandardCharsets.UTF_8));

        FileResource fileResource = Mockito.mock(FileResource.class, NOT_MOCKED_ANSWER);

        doReturn(inputStream).when(fileResource).createStream();

        String path = "/a/b/c/d.xml";

        boolean result = plugin.accepts(fileResource, path, DUMMY_SCOPE);

        assertThat(result).isEqualTo(false);
    }

    @Test
    void scanReturnsResultWhenScannerReturnsDescriptor() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();
        plugin.initialize();

        FileResource fileResource = mock(FileResource.class);
        InputStream stream = new ByteArrayInputStream("<project/>".getBytes(StandardCharsets.UTF_8));
        doReturn(stream).when(fileResource).createStream();
        doReturn(new File("pom.xml")).when(fileResource).getFile();

        MavenPomXmlDescriptor inputDescriptor = mock(MavenPomXmlDescriptor.class);
        MavenPomXmlDescriptor scannedDescriptor = mock(MavenPomXmlDescriptor.class);

        Scanner scanner = mock(Scanner.class);
        ScannerContext context = mock(ScannerContext.class);
        when(scanner.getContext()).thenReturn(context);
        when(context.peekOrDefault(eq(PomModelBuilder.class), isNull())).thenReturn(null);
        when(scanner.scan(any(Model.class), anyString(), any(Scope.class))).thenReturn(scannedDescriptor);

        MavenPomXmlDescriptor result = plugin.scan(fileResource, inputDescriptor, "/pom.xml", DUMMY_SCOPE, scanner);

        assertThat(result).isSameAs(scannedDescriptor);
        verify(scannedDescriptor).setValid(true);
        verify(inputDescriptor, never()).setValid(anyBoolean());
    }

    @Test
    void scanReturnsInputDescriptorWhenScannerReturnsNull() throws Exception {
        MavenPomFileScannerPlugin plugin = new MavenPomFileScannerPlugin();
        plugin.initialize();

        FileResource fileResource = mock(FileResource.class);
        InputStream stream = new ByteArrayInputStream("<project/>".getBytes(StandardCharsets.UTF_8));
        doReturn(stream).when(fileResource).createStream();
        doReturn(new File("pom.xml")).when(fileResource).getFile();

        MavenPomXmlDescriptor inputDescriptor = mock(MavenPomXmlDescriptor.class);

        Scanner scanner = mock(Scanner.class);
        ScannerContext context = mock(ScannerContext.class);
        when(scanner.getContext()).thenReturn(context);
        when(context.peekOrDefault(eq(PomModelBuilder.class), isNull())).thenReturn(null);
        when(scanner.scan(any(Model.class), anyString(), any(Scope.class))).thenReturn(null);

        MavenPomXmlDescriptor result = plugin.scan(fileResource, inputDescriptor, "/pom.xml", DUMMY_SCOPE, scanner);

        assertThat(result).isSameAs(inputDescriptor);
        verify(inputDescriptor).setValid(false);
    }

    private static class MethodNotMockedAnswer implements Answer {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Method calledMethod = invocation.getMethod();
            String signature = calledMethod.toGenericString();

            throw new RuntimeException(signature + " is not mocked!");
        }
    }

    private static class DummyScope implements Scope {
        @Override
        public String getPrefix() {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public String getName() {
            throw new RuntimeException("Not implemented yet!");
        }
    }

}
