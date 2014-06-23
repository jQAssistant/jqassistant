package com.buschmais.jqassistant.scm.maven.test;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static org.mockito.Mockito.mock;
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
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.ClassesDirectory;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.scanner.MavenProjectMavenScannerPlugin;

public class MavenProjectScannerPluginTest {

    @Test
    public void projectScannerPlugin() throws IOException {
        MavenProjectMavenScannerPlugin scannerPlugin = new MavenProjectMavenScannerPlugin();

        // Mock parent project
        MavenProject parentProject = mock(MavenProject.class);
        Artifact parentArtifact = new DefaultArtifact("group", "parent-artifact", VersionRange.createFromVersion("1.0.0"), null, "pom", "main", null);
        when(parentProject.getArtifact()).thenReturn(parentArtifact);
        when(parentProject.getPackaging()).thenReturn("pom");

        // Mock project
        MavenProject project = mock(MavenProject.class);
        when(project.getName()).thenReturn("project");
        Artifact artifact = new DefaultArtifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), null, "jar", "main", null);
        when(project.getArtifact()).thenReturn(artifact);
        when(project.getPackaging()).thenReturn("jar");
        File basedir = mock(File.class);
        when(basedir.getAbsolutePath()).thenReturn("basedir");
        when(project.getBasedir()).thenReturn(basedir);
        when(project.getParent()).thenReturn(parentProject);

        Build build = new Build();
        build.setOutputDirectory("target/classes");
        build.setTestOutputDirectory("target/test-classes");
        when(project.getBuild()).thenReturn(build);
        Map<String, Object> properties = new HashMap<>();
        properties.put(MavenProject.class.getName(), project);
        Store store = mock(Store.class);
        MavenProjectDirectoryDescriptor projectDescriptor = mock(MavenProjectDirectoryDescriptor.class);
        when(store.find(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0")).thenReturn(null, projectDescriptor);
        when(store.create(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0")).thenReturn(projectDescriptor);

        ArtifactDirectoryDescriptor mainArtifact = mock(ArtifactDirectoryDescriptor.class);
        when(store.find(ArtifactDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(null, mainArtifact);
        when(store.create(ArtifactDirectoryDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(mainArtifact);

        ArtifactDirectoryDescriptor testArtifact = mock(ArtifactDirectoryDescriptor.class);
        when(store.find(ArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(null, testArtifact);
        when(store.create(ArtifactDirectoryDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(testArtifact);

        DependsOnDescriptor dependsOnDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(testArtifact, DependsOnDescriptor.class, mainArtifact)).thenReturn(dependsOnDescriptor);

        MavenProjectDescriptor parentProjectDescriptor = mock(MavenProjectDescriptor.class);
        when(store.find(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(null, parentProjectDescriptor);
        when(store.create(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(parentProjectDescriptor);

        scannerPlugin.initialize(store, properties);
        Scanner scanner = mock(Scanner.class);
        List mainFiles = new ArrayList<>();
        mainFiles.add(mock(FileDescriptor.class));
        List testFiles = new ArrayList<>();
        testFiles.add(mock(FileDescriptor.class));

        when(scanner.scan(Mockito.any(ClassesDirectory.class), Mockito.any(String.class), Mockito.eq(CLASSPATH))).thenReturn(mainFiles, testFiles);

        scannerPlugin.scan(project, null, null, scanner);

        verify(scanner).scan(Mockito.any(ClassesDirectory.class), Mockito.eq("target/classes"), Mockito.eq(CLASSPATH));
        verify(scanner).scan(Mockito.any(ClassesDirectory.class), Mockito.eq("target/test-classes"), Mockito.eq(CLASSPATH));
        verify(store).create(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0");
        verify(projectDescriptor).setName("project");
        verify(projectDescriptor).setGroupId("group");
        verify(projectDescriptor).setArtifactId("artifact");
        verify(projectDescriptor).setVersion("1.0.0");
        verify(projectDescriptor).setPackaging("jar");
        verify(projectDescriptor).setFileName("basedir");
        verify(store).create(ArtifactDirectoryDescriptor.class, "group:artifact:jar:main:1.0.0");
        verify(store).create(ArtifactDirectoryDescriptor.class, "group:artifact:test-jar:main:1.0.0");
        verify(store).create(testArtifact, DependsOnDescriptor.class, mainArtifact);
    }
}
