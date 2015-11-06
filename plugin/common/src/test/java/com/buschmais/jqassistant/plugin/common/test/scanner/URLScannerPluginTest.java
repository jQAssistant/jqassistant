package com.buschmais.jqassistant.plugin.common.test.scanner;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.UrlScannerPlugin;

@RunWith(MockitoJUnitRunner.class)
public class URLScannerPluginTest {

    private UrlScannerPlugin plugin = new UrlScannerPlugin();

    @Mock
    private Scanner scanner;

    @BeforeClass
    public static void registerURLHandler() {
        URL.setURLStreamHandlerFactory(new TestURLStreamHandlerFactory());
    }

    @Test
    public void urls() throws IOException {
        scan("test:/path", "test:/path");
        scan("test://myhost", "test://myhost");
        scan("test://myhost:8080", "test://myhost:8080");
        scan("test://myhost:8080/path", "test://myhost:8080/path");
        scan("test://myhost:8080/path?value1=test1&value2=test2", "test://myhost:8080/path?value1=test1&value2=test2");
        scan("test://myhost:8080/path?value1=test1&value2=test2#anchor", "test://myhost:8080/path?value1=test1&value2=test2#anchor");
    }

    @Test
    public void authentication() throws IOException {
        FileResource fileResource = scan("test://user:secret@myhost:8080/path?value1=test1&value2=test2#anchor",
                "test://myhost:8080/path?value1=test1&value2=test2#anchor");
        String content = IOUtils.toString(fileResource.createStream());
        assertThat(content, startsWith("Basic "));
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
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String authorization = getRequestProperty("Authorization");
            return new ByteArrayInputStream(authorization.getBytes());
        }
    }

    public static class TestURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL url) throws IOException {
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
