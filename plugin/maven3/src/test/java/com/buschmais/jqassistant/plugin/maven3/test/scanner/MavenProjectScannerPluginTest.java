package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.MavenProjectScannerPlugin;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

public class MavenProjectScannerPluginTest {

    @Test
    public void projectScannerPlugin() throws IOException {
        MavenProjectScannerPlugin scannerPlugin = new MavenProjectScannerPlugin();

        // Mock parent project
        MavenProject parentProject = mock(MavenProject.class);
        when(parentProject.getGroupId()).thenReturn("group");
        when(parentProject.getArtifactId()).thenReturn("parent-artifact");
        when(parentProject.getVersion()).thenReturn("1.0.0");
        when(parentProject.getPackaging()).thenReturn("jar");
        when(parentProject.getPackaging()).thenReturn("pom");

        // Mock project
        MavenProject project = mock(MavenProject.class);
        File pomXml = new File("pom.xml");
        when(project.getFile()).thenReturn(pomXml);
        when(project.getName()).thenReturn("project");
        Artifact artifact = new DefaultArtifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), null, "jar", "main", null);
        when(project.getGroupId()).thenReturn("group");
        when(project.getArtifactId()).thenReturn("artifact");
        when(project.getVersion()).thenReturn("1.0.0");
        when(project.getArtifact()).thenReturn(artifact);
        when(project.getPackaging()).thenReturn("jar");
        when(project.getParent()).thenReturn(parentProject);

        Set<Artifact> dependencies = new HashSet<>();
        Artifact dependency = new DefaultArtifact("group", "dependency", VersionRange.createFromVersion("2.0.0"), "compile", "jar", "main", null);
        dependencies.add(dependency);
        when(project.getDependencyArtifacts()).thenReturn(dependencies);

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

        // pom.xml
        MavenPomXmlDescriptor pomXmlDescriptor = mock(MavenPomXmlDescriptor.class);
        when(scanner.scan(pomXml, pomXml.getAbsolutePath(), MavenScope.PROJECT)).thenReturn(pomXmlDescriptor);

        // classes directory
        FileDescriptor classesDirectory = mock(FileDescriptor.class);
        final JavaClassesDirectoryDescriptor mainArtifact = mock(JavaClassesDirectoryDescriptor.class);
        final JavaClassesDirectoryDescriptor testArtifact = mock(JavaClassesDirectoryDescriptor.class);
        final JavaArtifactFileDescriptor dependencyArtifact = mock(JavaArtifactFileDescriptor.class);
        when(scanner.scan(Mockito.any(File.class), Mockito.eq("target/classes"), Mockito.eq(CLASSPATH))).thenReturn(classesDirectory);
        when(store.executeQuery(Mockito.anyString(), Mockito.anyMap())).thenAnswer(new Answer<Query.Result<CompositeRowObject>>() {
            @Override
            public Query.Result<CompositeRowObject> answer(InvocationOnMock invocation) throws Throwable {
                Query.Result<CompositeRowObject> result = mock(Query.Result.class);
                when(result.hasResult()).thenReturn(false);
                return result;
            }
        });
        when(store.create(JavaClassesDirectoryDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(mainArtifact);
        // test classes directory
        FileDescriptor testClassesDirectory = mock(FileDescriptor.class);
        when(scanner.scan(Mockito.any(File.class), Mockito.eq("target/test-classes"), Mockito.eq(CLASSPATH))).thenReturn(testClassesDirectory);
        when(store.create(JavaClassesDirectoryDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(testArtifact);
        when(store.create(JavaArtifactFileDescriptor.class, "group:dependency:jar:main:2.0.0")).thenReturn(dependencyArtifact);

        DependsOnDescriptor testDependsOnMainDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(testArtifact, DependsOnDescriptor.class, mainArtifact)).thenReturn(testDependsOnMainDescriptor);

        DependsOnDescriptor mainDependsOnDependencyDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(mainArtifact, DependsOnDescriptor.class, dependencyArtifact)).thenReturn(mainDependsOnDependencyDescriptor);

        DependsOnDescriptor testDependsOnDependencyDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(testArtifact, DependsOnDescriptor.class, dependencyArtifact)).thenReturn(testDependsOnDependencyDescriptor);

        MavenProjectDescriptor parentProjectDescriptor = mock(MavenProjectDescriptor.class);
        when(store.find(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(null, parentProjectDescriptor);
        when(store.create(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(parentProjectDescriptor);

        ScannerContext scannerContext = mock(ScannerContext.class);
        when(scannerContext.getStore()).thenReturn(store);
        when(scanner.getContext()).thenReturn(scannerContext);

        // scan
        scannerPlugin.initialize(properties);
        scannerPlugin.scan(project, null, null, scanner);

        // verify
        verify(scanner).scan(Mockito.any(File.class), Mockito.eq("target/classes"), Mockito.eq(CLASSPATH));
        verify(scanner).scan(Mockito.any(File.class), Mockito.eq("target/test-classes"), Mockito.eq(CLASSPATH));
        verify(store).create(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0");
        verify(projectDescriptor).setName("project");
        verify(projectDescriptor).setGroupId("group");
        verify(projectDescriptor).setArtifactId("artifact");
        verify(projectDescriptor).setVersion("1.0.0");
        verify(projectDescriptor).setPackaging("jar");
        verify(scanner).scan(pomXml, pomXml.getAbsolutePath(), MavenScope.PROJECT);
        verify(projectDescriptor).setModel(pomXmlDescriptor);
        verify(store).create(JavaClassesDirectoryDescriptor.class, "group:artifact:jar:main:1.0.0");
        verify(store).create(JavaClassesDirectoryDescriptor.class, "group:artifact:test-jar:main:1.0.0");
        verify(store).create(JavaArtifactFileDescriptor.class, "group:dependency:jar:main:2.0.0");
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
        verify(store).create(mainArtifact, DependsOnDescriptor.class, dependencyArtifact);
        verify(store).create(testArtifact, DependsOnDescriptor.class, dependencyArtifact);

        verify(scannerContext).push(JavaClassesDirectoryDescriptor.class, mainArtifact);
        verify(scannerContext).push(JavaClassesDirectoryDescriptor.class, testArtifact);
        verify(scannerContext, times(2)).pop(JavaClassesDirectoryDescriptor.class);
    }

}
