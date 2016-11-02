package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.ContainerFileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;
import com.buschmais.jqassistant.plugin.javaee6.impl.scanner.WarClassesResourceScannerPlugin;

@RunWith(MockitoJUnitRunner.class)
public class WarClassesResourceScannerPluginTest {

    @Mock
    private FileResource resource;

    @Mock
    private FileDescriptor fileDescriptor;

    @Mock
    private FileDescriptor containedFileDescriptor;

    @Mock
    private ClassFileDescriptor requiredFileDescriptor;

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private ContainerFileResolver containerFileResolver;

    @Before
    public void stub() {
        when(scanner.getContext()).thenReturn(scannerContext);
        when(scannerContext.peek(FileResolver.class)).thenReturn(containerFileResolver);
    }

    @Test
    public void accepts() throws IOException {
        WarClassesResourceScannerPlugin plugin = new WarClassesResourceScannerPlugin();
        assertThat(plugin.accepts(resource, "/Test.class", WebApplicationScope.WAR), equalTo(false));
        assertThat(plugin.accepts(resource, "/WEB-INF/classes/Test.class", WebApplicationScope.WAR), equalTo(true));
        assertThat(plugin.accepts(resource, "/WEB-INF/classes/Test.class", DefaultScope.NONE), equalTo(false));
    }

    @Test
    public void scan() throws IOException {
        when(scannerContext.getCurrentDescriptor()).thenReturn(fileDescriptor);
        when(scanner.scan(resource, fileDescriptor, "/Test.class", JavaScope.CLASSPATH)).thenReturn(containedFileDescriptor);

        WarClassesResourceScannerPlugin plugin = new WarClassesResourceScannerPlugin();
        FileDescriptor scan = plugin.scan(resource, "/WEB-INF/classes/Test.class", WebApplicationScope.WAR, scanner);

        assertThat(scan, is(containedFileDescriptor));
        verify(scanner).scan(resource, fileDescriptor, "/Test.class", JavaScope.CLASSPATH);
    }

}
