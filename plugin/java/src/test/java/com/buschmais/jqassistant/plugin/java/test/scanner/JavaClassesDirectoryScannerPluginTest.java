package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
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
    private FileResolver parentFileResolver;

    @Mock
    private Store store;

    private File directory;

    private JavaClassesDirectoryScannerPlugin plugin;

    @BeforeEach
    void before() throws IOException {
        File projectDirectory = Files.createTempDirectory("project")
            .toFile();
        directory = new File(projectDirectory, "directory");
        directory.mkdir();
        doReturn(context).when(scanner)
            .getContext();
        doReturn(projectDirectory).when(context)
            .getProjectDirectory();
        doReturn(store).when(context)
            .getStore();

        doAnswer(invocation -> {
            JavaArtifactFileDescriptor containerDescriptor = mock(invocation.<Class<JavaArtifactFileDescriptor>>getArgument(1));
            doReturn(new ArrayList<>()).when(containerDescriptor)
                .getProvides();
            doReturn(new ArrayList<>()).when(containerDescriptor)
                .getContains();
            doReturn(new ArrayList<>()).when(containerDescriptor)
                .getRequires();
            return containerDescriptor;
        }).when(parentFileResolver)
            .match(anyString(), eq(JavaArtifactFileDescriptor.class), any(ScannerContext.class));
        doReturn(parentFileResolver).when(context)
            .peek(FileResolver.class);

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
     * Verify that the plugin matches the artifact from the parent file resolver.
     *
     * @throws IOException
     *     If the test fails.
     */
    @Test
    void matchArtifact() throws IOException {
        JavaArtifactFileDescriptor descriptor = plugin.scan(directory, null, JavaScope.CLASSPATH, scanner);

        assertThat(descriptor).isNotNull();
        verify(parentFileResolver).match("/directory", JavaArtifactFileDescriptor.class, context);
    }
}
