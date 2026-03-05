package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenMainArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenTestArtifactDescriptor;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MavenWARProjectScannerPluginTest {

    public static final String WEBAPP = "src/main/webapp";

    @Mock
    private MavenProject mavenProject;

    @Mock
    private MavenProjectDirectoryDescriptor projectDirectoryDescriptor;

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private MainArtifactDescriptor mainArtifact;

    @Mock
    private TestArtifactDescriptor testArtifact;

    @Mock
    private FileDescriptor webapp;

    @Mock
    private List<FileDescriptor> projectContainsFiles;

    private final MavenWARProjectScannerPlugin plugin = new MavenWARProjectScannerPlugin();

    @Test
    void scanWithTypeResolver() throws IOException {
        doReturn(List.of(testArtifact, mainArtifact)).when(projectDirectoryDescriptor)
            .getCreatesArtifacts();

        verifyScan();

        verify(scannerContext).push(eq(TypeResolver.class), any(TypeResolver.class));
        verify(scannerContext).pop(TypeResolver.class);
    }

    @Test
    void scanWithoutTypeResolver() throws IOException {
        doReturn(List.of()).when(projectDirectoryDescriptor)
            .getCreatesArtifacts();

        verifyScan();

        verify(scannerContext, never()).push(eq(TypeResolver.class), any(TypeResolver.class));
        verify(scannerContext, never()).pop(TypeResolver.class);
    }

    private void verifyScan() throws IOException {
        File baseDir = new File("target/test-module");
        File webappDir = new File(baseDir, WEBAPP);
        webappDir.mkdirs();
        doReturn(baseDir).when(mavenProject)
            .getBasedir();
        doReturn(scannerContext).when(scanner)
            .getContext();
        doReturn(projectDirectoryDescriptor).when(scannerContext)
            .getCurrentDescriptor();
        doReturn(webapp).when(scanner)
            .scan(argThat((ArgumentMatcher<File>) f -> f.equals(webappDir)), eq(WEBAPP), eq(DefaultScope.NONE));
        doReturn(projectContainsFiles).when(projectDirectoryDescriptor)
            .getContains();

        plugin.scan(mavenProject, "web-module", DefaultScope.NONE, scanner);

        verify(scanner).scan(webappDir, WEBAPP, DefaultScope.NONE);
        verify(projectContainsFiles).add(webapp);
    }

    private interface MainArtifactDescriptor extends JavaArtifactFileDescriptor, MavenMainArtifactDescriptor {
    }

    private interface TestArtifactDescriptor extends JavaArtifactFileDescriptor, MavenTestArtifactDescriptor {
    }

}
