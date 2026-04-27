package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractContainerScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileContainerScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    @Mock
    private Cache cache;

    @BeforeEach
    public void stub() {
        when(scanner.getContext()).thenReturn(context);
        when(context.getStore()).thenReturn(store);
        when(store.create(FileDescriptor.class)).thenReturn(mock(FileDescriptor.class));
        doReturn(cache).when(store)
            .getCache(anyString());
        doAnswer((Answer<FileDescriptor>) invocation -> ((Function<String, FileDescriptor>) invocation.getArgument(1)).apply(invocation.getArgument(0))).when(
                cache)
            .get(anyString(), any(Function.class));
        Deque<FileResolver> fileResolvers = new LinkedList<>();

        doAnswer(invocation -> {
            FileResolver resolver = (FileResolver) invocation.getArguments()[1];
            fileResolvers.push(resolver);
            return null;
        }).when(context)
            .push(eq(FileResolver.class), any(FileResolver.class));

        when(context.peek(FileResolver.class)).then(invocation -> fileResolvers.peek());

        when(scanner.scan(any(FileResource.class), anyString(), eq(DefaultScope.NONE))).thenAnswer(invocation -> {
            String path = (String) invocation.getArguments()[1];
            if ("/reject".equals(path)) {
                return null;
            }
            FileResolver fileResolver = context.peek(FileResolver.class);
            fileResolver.require("/D", FileDescriptor.class, context);
            return fileResolver.match(path, FileDescriptor.class, context);
        });
    }

    @Test
    public void provides() throws IOException {
        TestFileContainerScannerPlugin scannerPlugin = new TestFileContainerScannerPlugin();
        ArtifactFileDescriptor artifactFileDescriptor = scannerPlugin.scan(Arrays.asList("A", "B", "C", "reject"), "/", DefaultScope.NONE, scanner);
        assertThat(artifactFileDescriptor).isNotNull();

        verify(artifactFileDescriptor).setFileName("/");

        List<FileDescriptor> provides = scannerPlugin.getContains();
        assertThat(provides.size()).isEqualTo(3);
        assertThat(provides).isEqualTo(artifactFileDescriptor.getContains());
        FileDescriptor a = provides.get(0);
        verify(a).setFileName("/A");
        FileDescriptor b = provides.get(1);
        verify(b).setFileName("/B");
        FileDescriptor c = provides.get(2);
        verify(c).setFileName("/C");

        assertThat(scannerPlugin.getContains()).isEqualTo(provides);

        List<FileDescriptor> requires = scannerPlugin.getRequires();
        assertThat(requires.size()).isEqualTo(1);
        assertThat(requires).isEqualTo(artifactFileDescriptor.getRequires());
        FileDescriptor requiredFileDescriptor = requires.get(0);
        verify(requiredFileDescriptor).setFileName("/D");
    }

    @Test
    public void requires() throws IOException {
        TestFileContainerScannerPlugin scannerPlugin = new TestFileContainerScannerPlugin();
        ArtifactFileDescriptor artifactFileDescriptor = scannerPlugin.scan(Arrays.asList("A", "D"), "/", DefaultScope.NONE, scanner);
        assertThat(artifactFileDescriptor).isNotNull();

        verify(artifactFileDescriptor).setFileName("/");

        List<FileDescriptor> contains = scannerPlugin.getContains();
        assertThat(contains.size()).isEqualTo(2);
        assertThat(contains).isEqualTo(artifactFileDescriptor.getContains());
        FileDescriptor a = contains.get(0);
        verify(a).setFileName("/A");
        FileDescriptor b = contains.get(1);
        verify(b).setFileName("/D");

        List<FileDescriptor> requires = scannerPlugin.getRequires();
        assertThat(requires.size()).isEqualTo(0);
        assertThat(requires).isEqualTo(artifactFileDescriptor.getRequires());
    }

    private static class TestFileContainerScannerPlugin extends AbstractContainerScannerPlugin<Collection<String>, String, ArtifactFileDescriptor> {

        private List<FileDescriptor> provides = new ArrayList<>();

        private List<FileDescriptor> contains = new ArrayList<>();

        private List<FileDescriptor> requires = new ArrayList<>();

        @Override
        public boolean accepts(Collection<String> item, String path, Scope scope) throws IOException {
            return true;
        }

        @Override
        protected ArtifactFileDescriptor getContainerDescriptor(Collection<String> container, ScannerContext scannerContext) {
            ArtifactFileDescriptor artifactFileDescriptor = mock(ArtifactFileDescriptor.class);
            when(artifactFileDescriptor.getContains()).thenReturn(provides);
            when(artifactFileDescriptor.getContains()).thenReturn(contains);
            when(artifactFileDescriptor.getRequires()).thenReturn(requires);
            return artifactFileDescriptor;
        }

        @Override
        protected Iterable<? extends String> getEntries(Collection<String> container) {
            return container;
        }

        @Override
        protected String getContainerPath(Collection<String> container, String path) {
            return path;
        }

        @Override
        protected String getRelativePath(Collection<String> container, String entry) {
            return "/" + entry;
        }

        @Override
        protected void enterContainer(Collection<String> container, ArtifactFileDescriptor artifactFileDescriptor, ScannerContext scannerContext) {

        }

        @Override
        protected void leaveContainer(Collection<String> container, ArtifactFileDescriptor artifactFileDescriptor, ScannerContext scannerContext) {

        }

        @Override
        protected Resource getEntry(Collection<String> container, String entry) {
            return mock(FileResource.class);
        }

        List<FileDescriptor> getProvides() {
            return provides;
        }

        List<FileDescriptor> getContains() {
            return contains;
        }

        List<FileDescriptor> getRequires() {
            return requires;
        }
    }
}
