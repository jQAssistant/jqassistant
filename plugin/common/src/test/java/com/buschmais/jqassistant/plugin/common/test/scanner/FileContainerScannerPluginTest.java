package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractContainerScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileContainerScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    @BeforeEach
    public void stub() {
        when(scanner.getContext()).thenReturn(context);
        when(context.getStore()).thenReturn(store);
        when(store.create(FileDescriptor.class)).thenReturn(mock(FileDescriptor.class));
        doAnswer((Answer<FileDescriptor>) invocation -> ((Function<String, FileDescriptor>) invocation.getArgument(2)).apply(invocation.getArgument(1)))
                .when(store).get(anyString(), anyString(), any(Function.class));
        Deque<FileResolver> fileResolvers = new LinkedList<>();

        Mockito.doAnswer(invocation -> {
            FileResolver resolver = (FileResolver) invocation.getArguments()[1];
            fileResolvers.push(resolver);
            return null;
        }).when(context).push(eq(FileResolver.class), any(FileResolver.class));

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
    public void contains() throws IOException {
        TestContainerScannerPlugin scannerPlugin = new TestContainerScannerPlugin();
        DirectoryDescriptor directoryDescriptor = scannerPlugin.scan(Arrays.asList("A", "B", "C", "reject"), "/", DefaultScope.NONE, scanner);
        assertThat(directoryDescriptor, notNullValue());

        verify(directoryDescriptor).setFileName("/");

        List<FileDescriptor> contains = scannerPlugin.getContains();
        assertThat(contains.size(), equalTo(3));
        assertThat(contains, equalTo(directoryDescriptor.getContains()));
        FileDescriptor a = contains.get(0);
        verify(a).setFileName("/A");
        FileDescriptor b = contains.get(1);
        verify(b).setFileName("/B");
        FileDescriptor c = contains.get(2);
        verify(c).setFileName("/C");

        List<FileDescriptor> requires = scannerPlugin.getRequires();
        assertThat(requires.size(), equalTo(1));
        assertThat(requires, equalTo(directoryDescriptor.getRequires()));
        FileDescriptor requiredFileDescriptor = requires.get(0);
        verify(requiredFileDescriptor).setFileName("/D");
    }

    @Test
    public void requires() throws IOException {
        TestContainerScannerPlugin scannerPlugin = new TestContainerScannerPlugin();
        DirectoryDescriptor directoryDescriptor = scannerPlugin.scan(Arrays.asList("A", "D"), "/", DefaultScope.NONE, scanner);
        assertThat(directoryDescriptor, notNullValue());

        verify(directoryDescriptor).setFileName("/");

        List<FileDescriptor> contains = scannerPlugin.getContains();
        assertThat(contains.size(), equalTo(2));
        assertThat(contains, equalTo(directoryDescriptor.getContains()));
        FileDescriptor a = contains.get(0);
        verify(a).setFileName("/A");
        FileDescriptor b = contains.get(1);
        verify(b).setFileName("/D");

        List<FileDescriptor> requires = scannerPlugin.getRequires();
        assertThat(requires.size(), equalTo(0));
        assertThat(requires, equalTo(directoryDescriptor.getRequires()));
    }

    private static class TestContainerScannerPlugin extends AbstractContainerScannerPlugin<Collection<String>, String, DirectoryDescriptor> {

        private List<FileDescriptor> contains = new ArrayList<>();

        private List<FileDescriptor> requires = new ArrayList<>();

        @Override
        public boolean accepts(Collection<String> item, String path, Scope scope) throws IOException {
            return true;
        }

        @Override
        protected DirectoryDescriptor getContainerDescriptor(Collection<String> container, ScannerContext scannerContext) {
            DirectoryDescriptor directoryDescriptor = mock(DirectoryDescriptor.class);
            when(directoryDescriptor.getContains()).thenReturn(contains);
            when(directoryDescriptor.getRequires()).thenReturn(requires);
            return directoryDescriptor;
        }

        @Override
        protected Iterable<? extends String> getEntries(Collection<String> container) throws IOException {
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
        protected void enterContainer(Collection<String> container, DirectoryDescriptor containerDescriptor, ScannerContext scannerContext) throws IOException {

        }

        @Override
        protected void leaveContainer(Collection<String> container, DirectoryDescriptor containerDescriptor, ScannerContext scannerContext) throws IOException {

        }

        @Override
        protected Resource getEntry(Collection<String> container, String entry) {
            return mock(FileResource.class);
        }

        public List<FileDescriptor> getContains() {
            return contains;
        }

        public List<FileDescriptor> getRequires() {
            return requires;
        }
    }
}
