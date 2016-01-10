package com.buschmais.jqassistant.plugin.common.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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

@RunWith(MockitoJUnitRunner.class)
public class FileContainerScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    @Before
    public void stub() {
        when(scanner.getContext()).thenReturn(context);
        when(context.getStore()).thenReturn(store);
        when(store.create(FileDescriptor.class)).thenReturn(mock(FileDescriptor.class));
        when(store.addDescriptorType(Mockito.any(FileDescriptor.class), Mockito.eq(FileDescriptor.class))).thenReturn(mock(FileDescriptor.class));
        final Deque<FileResolver> fileResolvers = new LinkedList<>();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                FileResolver resolver = (FileResolver) invocation.getArguments()[1];
                fileResolvers.push(resolver);
                return null;
            }
        }).when(context).push(eq(FileResolver.class), any(FileResolver.class));
        when(context.peek(FileResolver.class)).then(new Answer<FileResolver>() {
            @Override
            public FileResolver answer(InvocationOnMock invocation) throws Throwable {
                return fileResolvers.peek();
            }
        });
        when(scanner.scan(Mockito.anyString(), Mockito.anyString(), Mockito.eq(DefaultScope.NONE))).thenAnswer(new Answer<FileDescriptor>() {
            @Override
            public FileDescriptor answer(InvocationOnMock invocation) throws Throwable {
                FileResolver fileResolver = context.peek(FileResolver.class);
                fileResolver.require("/D", FileDescriptor.class, context);
                String path = (String) invocation.getArguments()[1];
                return fileResolver.match(path, FileDescriptor.class, context);
            }
        });
    }

    @Test
    public void contains() throws IOException {
        TestContainerScannerPlugin scannerPlugin = new TestContainerScannerPlugin();
        DirectoryDescriptor directoryDescriptor = scannerPlugin.scan(Arrays.asList("A", "B", "C"), "/", DefaultScope.NONE, scanner);
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

    private static class TestContainerScannerPlugin
            extends AbstractContainerScannerPlugin<Collection<String>, String, DirectoryDescriptor> {

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
