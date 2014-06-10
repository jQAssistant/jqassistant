package com.buschmais.jqassistant.scm.maven.test;

import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.MavenProjectScannerPlugin;

public class MavenProjectScannerPluginTest {

    @Test
    public void projectScannerPlugin() throws IOException {
        MavenProjectScannerPlugin scannerPlugin = new MavenProjectScannerPlugin();
        MavenProject project = mock(MavenProject.class);
        Artifact artifact = new DefaultArtifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), null, "jar", "main", null);
        when(project.getArtifact()).thenReturn(artifact);
        Build build = new Build();
        build.setOutputDirectory("target/classes");
        build.setTestOutputDirectory("target/test-classes");
        when(project.getBuild()).thenReturn(build);
        Map<String, Object> properties = new HashMap<>();
        properties.put(MavenProject.class.getName(), project);
        Store store = mock(Store.class);
        ArtifactDescriptor mainArtifact = mock(ArtifactDescriptor.class);
        when(store.find(ArtifactDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(null, mainArtifact);
        when(store.create(ArtifactDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(mainArtifact);
        ArtifactDescriptor testArtifact = mock(ArtifactDescriptor.class);
        when(store.find(ArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(null, testArtifact);
        when(store.create(ArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(testArtifact);
        scannerPlugin.initialize(store, properties);
        Scanner scanner = mock(Scanner.class);
        List mainFiles = new ArrayList<>();
        mainFiles.add(mock(FileDescriptor.class));
        List testFiles = new ArrayList<>();
        testFiles.add(mock(FileDescriptor.class));

        when(scanner.scan(Mockito.any(File.class), Mockito.eq(CLASSPATH))).thenReturn(mainFiles, testFiles);

        scannerPlugin.scan(project, null, null, scanner);

        verify(scanner, times(2)).scan(Mockito.any(File.class), Mockito.eq(CLASSPATH));
        verify(store).find(ArtifactDescriptor.class, "group:artifact:jar:main:1.0.0");
        verify(store).create(ArtifactDescriptor.class, "group:artifact:jar:main:1.0.0");
        verify(mainArtifact).addContains(Mockito.any(FileDescriptor.class));
        verify(store).find(ArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0");
        verify(store).create(ArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0");
        verify(testArtifact).addContains(Mockito.any(FileDescriptor.class));
    }
}
