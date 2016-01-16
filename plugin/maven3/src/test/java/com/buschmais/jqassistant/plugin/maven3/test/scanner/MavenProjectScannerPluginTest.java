package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;
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
        List<ArtifactFileDescriptor> createsArtifacts = new LinkedList<>();
        when(projectDescriptor.getCreatesArtifacts()).thenReturn(createsArtifacts);
        when(store.find(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0")).thenReturn(null, projectDescriptor);
        when(store.create(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0")).thenReturn(projectDescriptor);

        Scanner scanner = mock(Scanner.class);

        // pom.xml
        MavenPomXmlDescriptor pomXmlDescriptor = mock(MavenPomXmlDescriptor.class);
        when(scanner.scan(pomXml, pomXml.getAbsolutePath(), MavenScope.PROJECT)).thenReturn(pomXmlDescriptor);

        // Effective effective model
        MavenPomDescriptor effectiveModelDescriptor = mock(MavenPomDescriptor.class);
        when(store.create(MavenPomDescriptor.class)).thenReturn(effectiveModelDescriptor);
        Model effectiveModel = mock(Model.class);
        when(project.getModel()).thenReturn(effectiveModel);
        when(scanner.scan(effectiveModel, pomXml.getAbsolutePath(), MavenScope.PROJECT)).thenReturn(effectiveModelDescriptor);

        // classes directory
        MavenArtifactDescriptor mainArtifactDescriptor = mock(MavenArtifactDescriptor.class);
        JavaClassesDirectoryDescriptor mainClassesDirectory = mock(JavaClassesDirectoryDescriptor.class);
        MavenArtifactDescriptor testArtifactDescriptor = mock(MavenArtifactDescriptor.class);
        JavaClassesDirectoryDescriptor testClassesDirectory = mock(JavaClassesDirectoryDescriptor.class);
        MavenArtifactDescriptor dependencyArtifact = mock(MavenArtifactDescriptor.class);
        when(scanner.scan(any(File.class), eq("target/classes"), eq(CLASSPATH))).thenReturn(mainClassesDirectory);
        when(store.executeQuery(anyString(), anyMap())).thenAnswer(new Answer<Query.Result<CompositeRowObject>>() {
            @Override
            public Query.Result<CompositeRowObject> answer(InvocationOnMock invocation) throws Throwable {
                Query.Result<CompositeRowObject> result = mock(Query.Result.class);
                when(result.hasResult()).thenReturn(false);
                return result;
            }
        });
        when(store.create(MavenArtifactDescriptor.class, "group:artifact:jar:main:1.0.0")).thenReturn(mainArtifactDescriptor);
        when(store.addDescriptorType(mainArtifactDescriptor, JavaClassesDirectoryDescriptor.class)).thenReturn(mainClassesDirectory);
        // test classes directory
        when(scanner.scan(any(File.class), eq("target/test-classes"), eq(CLASSPATH))).thenReturn(testClassesDirectory);
        when(store.create(MavenArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0")).thenReturn(testArtifactDescriptor);
        when(store.addDescriptorType(testArtifactDescriptor, JavaClassesDirectoryDescriptor.class)).thenReturn(testClassesDirectory);
        when(store.create(MavenArtifactDescriptor.class, "group:dependency:jar:main:2.0.0")).thenReturn(dependencyArtifact);

        DependsOnDescriptor testDependsOnMainDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor)).thenReturn(testDependsOnMainDescriptor);

        DependsOnDescriptor mainDependsOnDependencyDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(mainArtifactDescriptor, DependsOnDescriptor.class, dependencyArtifact)).thenReturn(mainDependsOnDependencyDescriptor);

        DependsOnDescriptor testDependsOnDependencyDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(testArtifactDescriptor, DependsOnDescriptor.class, dependencyArtifact)).thenReturn(testDependsOnDependencyDescriptor);

        MavenProjectDescriptor parentProjectDescriptor = mock(MavenProjectDescriptor.class);
        when(store.find(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(null, parentProjectDescriptor);
        when(store.create(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(parentProjectDescriptor);

        ScannerContext scannerContext = mock(ScannerContext.class);
        when(scannerContext.getStore()).thenReturn(store);
        // when(scannerContext.peek(ArtifactResolver.class)).thenReturn(new
        // MavenArtifactResolver());
        when(scanner.getContext()).thenReturn(scannerContext);

        // scan
        scannerPlugin.configure(scannerContext, properties);
        scannerPlugin.scan(project, null, null, scanner);

        // verify
        verify(scanner).scan(any(File.class), eq("target/classes"), eq(CLASSPATH));
        verify(scanner).scan(any(File.class), eq("target/test-classes"), eq(CLASSPATH));
        verify(store).create(MavenProjectDirectoryDescriptor.class, "group:artifact:1.0.0");
        verify(projectDescriptor).setName("project");
        verify(projectDescriptor).setGroupId("group");
        verify(projectDescriptor).setArtifactId("artifact");
        verify(projectDescriptor).setVersion("1.0.0");
        verify(projectDescriptor).setPackaging("jar");
        // Model
        verify(scanner).scan(pomXml, pomXml.getAbsolutePath(), MavenScope.PROJECT);
        verify(projectDescriptor).setModel(pomXmlDescriptor);
        // Effective model
        verify(store).create(MavenPomDescriptor.class);
        verify(scannerContext).push(MavenPomDescriptor.class, effectiveModelDescriptor);
        verify(scanner).scan(effectiveModel, pomXml.getAbsolutePath(), MavenScope.PROJECT);
        verify(scannerContext).pop(MavenPomDescriptor.class);
        verify(projectDescriptor).setEffectiveModel(effectiveModelDescriptor);
        verify(store).create(MavenArtifactDescriptor.class, "group:artifact:jar:main:1.0.0");
        verify(store).addDescriptorType(mainArtifactDescriptor, JavaClassesDirectoryDescriptor.class);
        verify(store).create(MavenArtifactDescriptor.class, "group:artifact:test-jar:main:1.0.0");
        verify(store).addDescriptorType(testArtifactDescriptor, JavaClassesDirectoryDescriptor.class);
        verify(store).create(MavenArtifactDescriptor.class, "group:dependency:jar:main:2.0.0");
        verify(mainArtifactDescriptor).setGroup("group");
        verify(mainArtifactDescriptor).setName("artifact");
        verify(mainArtifactDescriptor).setType("jar");
        verify(mainArtifactDescriptor).setClassifier("main");
        verify(mainArtifactDescriptor).setVersion("1.0.0");
        verify(testArtifactDescriptor).setGroup("group");
        verify(testArtifactDescriptor).setName("artifact");
        verify(testArtifactDescriptor).setType("test-jar");
        verify(testArtifactDescriptor).setClassifier("main");
        verify(testArtifactDescriptor).setVersion("1.0.0");

        verify(store).create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor);
        verify(store).create(mainArtifactDescriptor, DependsOnDescriptor.class, dependencyArtifact);
        verify(store).create(testArtifactDescriptor, DependsOnDescriptor.class, dependencyArtifact);

        verify(scannerContext, times(2)).push(eq(TypeResolver.class), any(TypeResolver.class));
        verify(scannerContext, times(2)).pop(TypeResolver.class);

        verify(scannerContext).push(JavaArtifactFileDescriptor.class, mainClassesDirectory);
        verify(scannerContext).push(JavaArtifactFileDescriptor.class, testClassesDirectory);
        verify(scannerContext, times(2)).pop(JavaArtifactFileDescriptor.class);
        assertThat(createsArtifacts.size(), equalTo(2));
        assertThat(createsArtifacts, allOf(hasItem(mainArtifactDescriptor), hasItem(testArtifactDescriptor)));
    }

}
