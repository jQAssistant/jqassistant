package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MavenEARProjectScannerPluginTest {

    public static final String APPLICATION = "src/main/application";

    @Mock
    private MavenProject mavenProject;

    @Mock
    private MavenProjectDirectoryDescriptor projectDirectoryDescriptor;

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private FileDescriptor application;

    @Mock
    private List<FileDescriptor> projectContainsFiles;

    private final MavenEARProjectScannerPlugin plugin = new MavenEARProjectScannerPlugin();

    @Test
    void scanApplication() throws IOException {
        File baseDir = new File("target/test-module");
        File applicationDir = new File(baseDir, APPLICATION);
        applicationDir.mkdirs();
        doReturn(baseDir).when(mavenProject)
            .getBasedir();
        doReturn(scannerContext).when(scanner)
            .getContext();
        doReturn(projectDirectoryDescriptor).when(scannerContext)
            .getCurrentDescriptor();
        doReturn(application).when(scanner)
            .scan(Mockito.argThat((ArgumentMatcher<File>) f -> f.equals(applicationDir)), eq(APPLICATION), eq(DefaultScope.NONE));
        doReturn(projectContainsFiles).when(projectDirectoryDescriptor)
            .getContains();

        plugin.scan(mavenProject, "web-module", DefaultScope.NONE, scanner);

        verify(scanner).scan(applicationDir, APPLICATION, DefaultScope.NONE);
        verify(projectContainsFiles).add(application);
    }
}
