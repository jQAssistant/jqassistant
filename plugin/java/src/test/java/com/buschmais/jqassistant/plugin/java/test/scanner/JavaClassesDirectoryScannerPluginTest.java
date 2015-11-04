package com.buschmais.jqassistant.plugin.java.test.scanner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.JavaClassesDirectoryScannerPlugin;

@RunWith(MockitoJUnitRunner.class)
public class JavaClassesDirectoryScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    private File directory;

    @Before
    public void before() throws IOException {
        when(scanner.getContext()).thenReturn(context);
        when(context.getStore()).thenReturn(store);
        directory = Files.createTempDirectory("directory").toFile();
    }

    @After
    public void after() throws IOException {
        if (directory != null) {
            directory.delete();
        }
    }

    /**
     * Verify that the plugin creates a new artifact if none exists in the
     * context.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void createArtifact() throws IOException {
        JavaClassesDirectoryScannerPlugin plugin = new JavaClassesDirectoryScannerPlugin();
        when(context.peek(FileResolver.class)).thenReturn(mock(FileResolver.class));
        JavaClassesDirectoryDescriptor artifact = mock(JavaClassesDirectoryDescriptor.class);
        when(context.peekOrDefault(JavaArtifactFileDescriptor.class, null)).thenReturn(null);
        when(store.create(JavaClassesDirectoryDescriptor.class)).thenReturn(artifact);

        JavaClassesDirectoryDescriptor descriptor = plugin.scan(directory, "/", JavaScope.CLASSPATH, scanner);

        verify(context).peekOrDefault(JavaArtifactFileDescriptor.class, null);
        verify(store).create(JavaClassesDirectoryDescriptor.class);

        assertThat(descriptor, is(artifact));
    }

    /**
     * Verify that the plugin re-uses an existing artifact which exists in the
     * context (e.g. for dependency resolution).
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void useArtifactFromContext() throws IOException {
        JavaClassesDirectoryScannerPlugin plugin = new JavaClassesDirectoryScannerPlugin();
        File directory = Files.createTempDirectory("directory").toFile();
        when(context.peek(FileResolver.class)).thenReturn(mock(FileResolver.class));
        JavaClassesDirectoryDescriptor artifact = mock(JavaClassesDirectoryDescriptor.class);
        when(context.peekOrDefault(JavaArtifactFileDescriptor.class, null)).thenReturn(artifact);
        when(store.addDescriptorType(artifact, JavaClassesDirectoryDescriptor.class)).thenReturn(mock(JavaClassesDirectoryDescriptor.class));

        JavaClassesDirectoryDescriptor descriptor = plugin.scan(directory, "/", JavaScope.CLASSPATH, scanner);

        verify(context).peekOrDefault(JavaArtifactFileDescriptor.class, null);
        verify(store, never()).create(JavaClassesDirectoryDescriptor.class);

        assertThat(descriptor, is(artifact));
    }
}
