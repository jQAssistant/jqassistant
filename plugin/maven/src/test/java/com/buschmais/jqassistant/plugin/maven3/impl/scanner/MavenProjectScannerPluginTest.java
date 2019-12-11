package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.EffectiveModel;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.MavenArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency.DependencyScanner;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.eclipse.aether.RepositorySystemSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MavenProjectScannerPluginTest {

    @Mock
    private Store store;

    @Mock
    private MavenSession mavenSession;

    @Mock
    private MavenArtifactResolver mavenArtifactResolver;

    @Mock
    private DependencyScanner dependencyScanner;

    @Mock
    private RepositorySystemSession repositorySystemSession;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Captor
    ArgumentCaptor<EffectiveModel> effectiveModelCaptor;

    @Test
    public void projectScannerPlugin() throws DependencyGraphBuilderException {
        MavenProjectScannerPlugin scannerPlugin = new MavenProjectScannerPlugin(dependencyScanner);
        doReturn(mavenArtifactResolver).when(scannerContext).peek(ArtifactResolver.class);

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
        File artifactFile = mock(File.class);
        lenient().doReturn("/artifact").when(artifactFile).getAbsolutePath();
        Artifact artifact = new DefaultArtifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), null, "jar", "main", null);
        artifact.setFile(artifactFile);
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
        MavenProjectDirectoryDescriptor projectDescriptor = mock(MavenProjectDirectoryDescriptor.class);
        List<ArtifactDescriptor> createsArtifacts = new LinkedList<>();
        when(projectDescriptor.getCreatesArtifacts()).thenReturn(createsArtifacts);
        when(store.find(MavenProjectDescriptor.class, "group:artifact:1.0.0")).thenReturn(null, projectDescriptor);
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
        doReturn(effectiveModelDescriptor).when(scanner).scan(any(Model.class), eq(pomXml.getAbsolutePath()), eq(MavenScope.PROJECT));

        // classes directory
        MavenMainArtifactDescriptor mainArtifactDescriptor = mock(MavenMainArtifactDescriptor.class);
        JavaClassesDirectoryDescriptor mainClassesDirectory = mock(JavaClassesDirectoryDescriptor.class);
        MavenTestArtifactDescriptor testArtifactDescriptor = mock(MavenTestArtifactDescriptor.class);
        JavaClassesDirectoryDescriptor testClassesDirectory = mock(JavaClassesDirectoryDescriptor.class);
        MavenArtifactDescriptor dependencyArtifact = mock(MavenArtifactDescriptor.class);
        ArgumentMatcher<Coordinates> mainArtifactCoordinatesMatcher = a -> a.getGroup().equals("group") && a.getName().equals("artifact")
                && a.getType().equals("jar");
        doReturn(mainArtifactDescriptor).when(mavenArtifactResolver).resolve(argThat(mainArtifactCoordinatesMatcher), eq(scannerContext));
        when(scanner.scan(any(File.class), eq("target/classes"), eq(CLASSPATH))).thenReturn(mainClassesDirectory);
        when(store.addDescriptorType(mainArtifactDescriptor, MavenMainArtifactDescriptor.class)).thenReturn(mainArtifactDescriptor);
        when(store.addDescriptorType(mainArtifactDescriptor, JavaClassesDirectoryDescriptor.class)).thenReturn(mainClassesDirectory);

        // test classes directory
        when(scanner.scan(any(File.class), eq("target/test-classes"), eq(CLASSPATH))).thenReturn(testClassesDirectory);
        ArgumentMatcher<Coordinates> testArtifactCoordinatesMatcher = a -> a.getGroup().equals("group") && a.getName().equals("artifact")
                && a.getType().equals("test-jar");
        doReturn(testArtifactDescriptor).when(mavenArtifactResolver).resolve(argThat(testArtifactCoordinatesMatcher), eq(scannerContext));
        when(store.addDescriptorType(testArtifactDescriptor, JavaClassesDirectoryDescriptor.class)).thenReturn(testClassesDirectory);
        when(store.addDescriptorType(testArtifactDescriptor, MavenTestArtifactDescriptor.class)).thenReturn(testArtifactDescriptor);
        when(store.addDescriptorType(mainArtifactDescriptor, JavaClassesDirectoryDescriptor.class)).thenReturn(mainClassesDirectory);

        doReturn(dependencyGraphBuilder).when(scannerContext).peek(DependencyGraphBuilder.class);
        DependencyNode dependencyNode = mock(DependencyNode.class);
        doReturn(dependencyNode).when(dependencyGraphBuilder).buildDependencyGraph(any(ProjectBuildingRequest.class), eq(null));

        // dependency artifacts

        DependsOnDescriptor testDependsOnMainDescriptor = mock(DependsOnDescriptor.class);
        when(store.create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor)).thenReturn(testDependsOnMainDescriptor);

        MavenProjectDescriptor parentProjectDescriptor = mock(MavenProjectDescriptor.class);
        when(store.find(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(null, parentProjectDescriptor);
        when(store.create(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0")).thenReturn(parentProjectDescriptor);

        ProjectBuildingRequest projectBuildingRequest = mock(ProjectBuildingRequest.class);
        doReturn(repositorySystemSession).when(projectBuildingRequest).getRepositorySession();
        doReturn(projectBuildingRequest).when(mavenSession).getProjectBuildingRequest();

        when(scannerContext.peek(MavenSession.class)).thenReturn(mavenSession);
        when(scannerContext.getStore()).thenReturn(store);
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
        verify(scanner, atLeastOnce()).scan(effectiveModelCaptor.capture(), eq(pomXml.getAbsolutePath()), eq(MavenScope.PROJECT));
        assertThat(effectiveModelCaptor.getValue().getDelegate(), is(effectiveModel));
        verify(scannerContext).pop(MavenPomDescriptor.class);
        verify(projectDescriptor).setEffectiveModel(effectiveModelDescriptor);
        verify(mavenArtifactResolver).resolve(argThat(mainArtifactCoordinatesMatcher), eq(scannerContext));
        verify(store).addDescriptorType(mainArtifactDescriptor, JavaClassesDirectoryDescriptor.class);
        verify(mavenArtifactResolver).resolve(argThat(testArtifactCoordinatesMatcher), eq(scannerContext));
        verify(store).addDescriptorType(testArtifactDescriptor, JavaClassesDirectoryDescriptor.class);

        verify(dependencyGraphBuilder).buildDependencyGraph(any(ProjectBuildingRequest.class), eq(null));
        verify(dependencyScanner).evaluate(dependencyNode, mainArtifactDescriptor, testArtifactDescriptor, scanner);

        verify(store).create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor);

        verify(scannerContext).push(JavaArtifactFileDescriptor.class, mainClassesDirectory);
        verify(scannerContext).push(JavaArtifactFileDescriptor.class, testClassesDirectory);
        verify(scannerContext, times(2)).pop(JavaArtifactFileDescriptor.class);
        assertThat(createsArtifacts.size(), equalTo(2));
        assertThat(createsArtifacts, hasItem(mainArtifactDescriptor));
        assertThat(createsArtifacts, hasItem(testArtifactDescriptor));
    }
}
