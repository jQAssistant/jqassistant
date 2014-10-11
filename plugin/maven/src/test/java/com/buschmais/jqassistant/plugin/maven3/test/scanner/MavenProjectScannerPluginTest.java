package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.scanner.MavenProjectScannerPlugin;

public class MavenProjectScannerPluginTest {

    @Test
    public void projectScannerPlugin() throws IOException {
        MavenProjectScannerPlugin scannerPlugin = new MavenProjectScannerPlugin();

        // Mock parent project
        MavenProject parentProject = mock(MavenProject.class);
        Artifact parentArtifact = new DefaultArtifact("group", "parent-artifact", VersionRange.createFromVersion("1.0.0"), null, "pom", "main", null);
        when(parentProject.getGroupId()).thenReturn("group");
        when(parentProject.getArtifactId()).thenReturn("parent-artifact");
        when(parentProject.getVersion()).thenReturn("1.0.0");
        when(parentProject.getPackaging()).thenReturn("jar");
        when(parentProject.getPackaging()).thenReturn("pom");

        // Mock project
        MavenProject project = mock(MavenProject.class);
        when(project.getName()).thenReturn("project");
        Artifact artifact = new DefaultArtifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), null, "jar", "main", null);
        when(project.getGroupId()).thenReturn("group");
        when(project.getArtifactId()).thenReturn("artifact");
        when(project.getVersion()).thenReturn("1.0.0");
        when(project.getArtifact()).thenReturn(artifact);
        when(project.getPackaging()).thenReturn("jar");
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

        Scanner scanner = mock(Scanner.class);

        FileDescriptor classesDirectory = mock(FileDescriptor.class);
        ArtifactDirectoryDescriptor mainArtifact = mock(ArtifactDirectoryDescriptor.class);
        when(scanner.scan(Mockito.any(File.class), Mockito.eq("target/classes"), Mockito.eq(CLASSPATH))).thenReturn(classesDirectory);
        when(store.find(ArtifactDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(null, mainArtifact);
        when(store.create(ArtifactDirectoryDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(mainArtifact);

        FileDescriptor testClassesDirectory = mock(FileDescriptor.class);
        ArtifactDirectoryDescriptor testArtifact = mock(ArtifactDirectoryDescriptor.class);
        when(scanner.scan(Mockito.any(File.class), Mockito.eq("target/test-classes"), Mockito.eq(CLASSPATH))).thenReturn(testClassesDirectory);
        when(store.find(ArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(null, testArtifact);
        when(store.create(ArtifactDirectoryDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(testArtifact);

        DependsOnDescriptor dependsOnDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(testArtifact, DependsOnDescriptor.class, mainArtifact)).thenReturn(dependsOnDescriptor);

        MavenProjectDescriptor parentProjectDescriptor = mock(MavenProjectDescriptor.class);
        when(store.find(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(null, parentProjectDescriptor);
        when(store.create(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(parentProjectDescriptor);

        ScannerContext scannerContext = mock(ScannerContext.class);
        when(scannerContext.getStore()).thenReturn(store);
        when(scanner.getContext()).thenReturn(scannerContext);

        scannerPlugin.initialize(properties);
        scannerPlugin.scan(project, null, null, scanner);

        verify(scanner).scan(Mockito.any(File.class), Mockito.eq("target/classes"), Mockito.eq(CLASSPATH));
        verify(scanner).scan(Mockito.any(File.class), Mockito.eq("target/test-classes"), Mockito.eq(CLASSPATH));
        verify(store).create(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0");
        verify(projectDescriptor).setName("project");
        verify(projectDescriptor).setGroupId("group");
        verify(projectDescriptor).setArtifactId("artifact");
        verify(projectDescriptor).setVersion("1.0.0");
        verify(projectDescriptor).setPackaging("jar");
        verify(store).create(ArtifactDirectoryDescriptor.class, "group:artifact:jar:main:1.0.0");
        verify(store).create(ArtifactDirectoryDescriptor.class, "group:artifact:test-jar:main:1.0.0");
        verify(mainArtifact).setGroup("group");
        verify(mainArtifact).setName("artifact");
        verify(mainArtifact).setType("jar");
        verify(mainArtifact).setClassifier("main");
        verify(mainArtifact).setVersion("1.0.0");
        verify(testArtifact).setGroup("group");
        verify(testArtifact).setName("artifact");
        verify(testArtifact).setType("test-jar");
        verify(testArtifact).setClassifier("main");
        verify(testArtifact).setVersion("1.0.0");

        verify(store).create(testArtifact, DependsOnDescriptor.class, mainArtifact);
    }
}
