package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.JavaClassesDirectoryScannerPlugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaClassesDirectoryScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    private File directory;

    private JavaClassesDirectoryScannerPlugin plugin;

    @BeforeEach
    void before() throws IOException {
        when(scanner.getContext()).thenReturn(context);
        when(context.getStore()).thenReturn(store);
        directory = Files.createTempDirectory("directory").toFile();
        this.plugin = new JavaClassesDirectoryScannerPlugin();
        this.plugin.configure(context, emptyMap());
    }

    @AfterEach
    void after() {
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
    void createArtifact() throws IOException {
        JavaClassesDirectoryDescriptor artifact = mock(JavaClassesDirectoryDescriptor.class);
        when(context.peekOrDefault(JavaArtifactFileDescriptor.class, null)).thenReturn(null);
        when(store.create(JavaClassesDirectoryDescriptor.class)).thenReturn(artifact);

        JavaClassesDirectoryDescriptor descriptor = plugin.scan(directory, "/", JavaScope.CLASSPATH, scanner);

        verify(context).peekOrDefault(JavaArtifactFileDescriptor.class, null);
        verify(store).create(JavaClassesDirectoryDescriptor.class);

        assertThat(descriptor).isEqualTo(artifact);
    }

    /**
     * Verify that the plugin re-uses an existing artifact which exists in the
     * context (e.g. for dependency resolution).
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void useArtifactFromContext() throws IOException {
        File directory = Files.createTempDirectory("directory").toFile();
        JavaClassesDirectoryDescriptor artifact = mock(JavaClassesDirectoryDescriptor.class);
        when(context.peekOrDefault(JavaArtifactFileDescriptor.class, null)).thenReturn(artifact);

        JavaClassesDirectoryDescriptor descriptor = plugin.scan(directory, "/", JavaScope.CLASSPATH, scanner);

        verify(context).peekOrDefault(JavaArtifactFileDescriptor.class, null);
        verify(store, never()).create(JavaClassesDirectoryDescriptor.class);

        assertThat(descriptor).isEqualTo(artifact);
    }
}
