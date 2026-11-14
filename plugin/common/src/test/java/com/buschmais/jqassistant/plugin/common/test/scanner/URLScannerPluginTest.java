package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.URLDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.URLScannerPlugin;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class URLScannerPluginTest {

    private final URLScannerPlugin plugin = new URLScannerPlugin();

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    @Mock
    private FileDescriptor fileDescriptor;

    @BeforeAll
    static void registerURLHandler() {
        URL.setURLStreamHandlerFactory(new TestURLStreamHandlerFactory());
    }

    @BeforeEach
    void setUp() {
        doReturn(context).when(scanner)
            .getContext();
        doReturn(store).when(context)
            .getStore();
        doReturn(fileDescriptor).when(scanner)
            .scan(any(), any(), any());
        doAnswer(i -> mock(FileDescriptor.class, withSettings().extraInterfaces(URLDescriptor.class))).when(store)
            .addDescriptorType(eq(fileDescriptor), eq(URLDescriptor.class));
    }

    @ParameterizedTest
    @MethodSource("urlArguments")
    void urls(String url, String expectedFileName) throws IOException {
        scanTestURL(new URL(url), expectedFileName);
    }

    static Stream<Arguments> urlArguments() {
        return Stream.of( //
            of("test:/path", "/path"), //
            of("test://myhost", ""), //
            of("test://myhost:8080/path", "/path"), //
            of("test://myhost:8080/path?value1=test1&value2=test2", "/path?value1=test1&value2=test2"));
    }

    @Test
    void authentication() throws IOException {
        File file = scanTestURL(new URL("test://user:secret@myhost:8080/path?value1=test1&value2=test2"), "/path?value1=test1&value2=test2");
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertThat(content).startsWith("Basic ");
    }

    @Test
    void getFileFromResource() throws IOException {
        URL url = URLScannerPluginTest.class.getResource("/test-resource.txt");

        URLDescriptor urlDescriptor = plugin.scan(url, url.toString(), DefaultScope.NONE, scanner);

        assertThat(urlDescriptor).isNotNull()
            .isInstanceOf(URLDescriptor.class).isInstanceOf(FileDescriptor.class);
        ArgumentCaptor<File> fileArgumentCaptor = ArgumentCaptor.forClass(File.class);
        verify(scanner).scan(fileArgumentCaptor.capture(), eq(null), eq(DefaultScope.NONE));
        assertThat(fileArgumentCaptor.getValue().getPath()).endsWith("test-resource.txt");
    }

    private File scanTestURL(URL url, String expectedFileName) throws IOException {
        URLDescriptor urlDescriptor = plugin.scan(url, url.toString(), DefaultScope.NONE, scanner);

        assertThat(urlDescriptor).isNotNull()
            .isInstanceOf(URLDescriptor.class).isInstanceOf(FileDescriptor.class);
        ArgumentCaptor<FileResource> fileResourceCaptor = ArgumentCaptor.forClass(FileResource.class);
        verify(scanner).scan(fileResourceCaptor.capture(), eq(expectedFileName), eq(DefaultScope.NONE));
        verify(store).addDescriptorType(fileDescriptor, URLDescriptor.class);
        return fileResourceCaptor.getValue().getFile();
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
            return new ByteArrayInputStream(authorization != null ? authorization.getBytes() : new byte[0]);
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
