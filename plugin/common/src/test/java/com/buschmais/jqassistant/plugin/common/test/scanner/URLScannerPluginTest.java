package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.URLScannerPlugin;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class URLScannerPluginTest {

    private URLScannerPlugin plugin = new URLScannerPlugin();

    @Mock
    private Scanner scanner;

    @BeforeAll
    static void registerURLHandler() {
        URL.setURLStreamHandlerFactory(new TestURLStreamHandlerFactory());
    }

    @Test
    void urls() throws IOException {
        scan("test:/path", "test:/path");
        scan("test://myhost", "test://myhost");
        scan("test://myhost:8080", "test://myhost:8080");
        scan("test://myhost:8080/path", "test://myhost:8080/path");
        scan("test://myhost:8080/path?value1=test1&value2=test2", "test://myhost:8080/path?value1=test1&value2=test2");
        scan("test://myhost:8080/path?value1=test1&value2=test2#anchor", "test://myhost:8080/path?value1=test1&value2=test2#anchor");
    }

    @Test
    void authentication() throws IOException {
        try (FileResource fileResource = scan("test://user:secret@myhost:8080/path?value1=test1&value2=test2#anchor",
            "test://myhost:8080/path?value1=test1&value2=test2#anchor")) {
            String content = IOUtils.toString(fileResource.createStream(), StandardCharsets.UTF_8);
            assertThat(content).startsWith("Basic ");
        }
    }

    @Test
    void getFileFromResource() throws IOException {
        String testResource = URLScannerPluginTest.class.getResource("/test-resource.txt")
            .toString();
        try (FileResource fileResource = scan(testResource, testResource)) {
            File file = fileResource.getFile();
            assertThat(file.getPath()).endsWith("/test-resource.txt");
        }
    }

    private FileResource scan(String path, String expectedPath) throws IOException {
        URL url = new URL(path);
        plugin.scan(url, path, DefaultScope.NONE, scanner);
        ArgumentCaptor<FileResource> resource = ArgumentCaptor.forClass(FileResource.class);
        verify(scanner).scan(resource.capture(), Mockito.eq(expectedPath), Mockito.eq(DefaultScope.NONE));
        return resource.getValue();
    }

    public static class TestURLConnection extends URLConnection {

        protected TestURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() {
        }

        @Override
        public InputStream getInputStream() {
            String authorization = getRequestProperty("Authorization");
            return new ByteArrayInputStream(authorization.getBytes());
        }
    }

    public static class TestURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL url) {
            return new TestURLConnection(url);
        }
    }

    public static class TestURLStreamHandlerFactory implements URLStreamHandlerFactory {
        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("test".equals(protocol)) {
                return new TestURLStreamHandler();
            }
            return null;
        }
    }
}
