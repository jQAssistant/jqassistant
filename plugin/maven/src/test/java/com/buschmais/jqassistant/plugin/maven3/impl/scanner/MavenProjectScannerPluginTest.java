package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactFilter;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenRepositoryArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.EffectiveModel;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency.DependencyScanner;

import com.github.benmanes.caffeine.cache.Cache;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
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
import org.eclipse.aether.repository.LocalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MavenProjectScannerPluginTest {

    private final LocalRepository localRepo = new LocalRepository("target/test/.m2");

    @Mock
    private Store store;

    @Mock
    private MavenSession mavenSession;

    @Mock
    private ArtifactRepository localRepository;

    @Mock
    private FileResolver fileResolver;

    @Mock
    private DependencyScanner dependencyScanner;

    @Mock
    private RepositorySystemSession repositorySystemSession;

    @Mock
    private Scan scanConfiguration;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Captor
    private ArgumentCaptor<EffectiveModel> effectiveModelCaptor;

    @Captor
    private ArgumentCaptor<ArtifactFilter> artifactFilterCaptor;

    @Test
    void scan() throws DependencyGraphBuilderException {
        scanAndVerify(new HashMap<>(), false);
    }

    @Test
    void scanDependencies() throws DependencyGraphBuilderException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("maven3.dependencies.scan", "true");
        properties.put("maven3.dependencies.includes", "included");
        properties.put("maven3.dependencies.excludes", "excluded");

        scanAndVerify(properties, true);

        ArtifactFilter artifactFilter = artifactFilterCaptor.getValue();
        assertThat(artifactFilter.getIncludes(), hasItems("included"));
        assertThat(artifactFilter.getExcludes(), hasItems("excluded"));
    }

    private void scanAndVerify(Map<String, Object> properties, boolean expectedDependenciesScan) throws DependencyGraphBuilderException {
        MavenProjectScannerPlugin scannerPlugin = new MavenProjectScannerPlugin(dependencyScanner);
        doReturn(fileResolver).when(scannerContext)
            .peek(FileResolver.class);

        // Mock parent project
        MavenProject parentProject = mock(MavenProject.class);
        when(parentProject.getGroupId()).thenReturn("group");
        when(parentProject.getArtifactId()).thenReturn("parent-artifact");
        when(parentProject.getVersion()).thenReturn("1.0.0");
        when(parentProject.getPackaging()).thenReturn("jar");
        when(parentProject.getPackaging()).thenReturn("pom");

        // Mock project
        MavenProject project = mock(MavenProject.class);
        doReturn(new File("/project")).when(project)
            .getBasedir();
        File pomXml = new File("pom.xml");
        when(project.getFile()).thenReturn(pomXml);
        when(project.getName()).thenReturn("project");
        File artifactFile = mock(File.class);
        lenient().doReturn("/artifact")
            .when(artifactFile)
            .getAbsolutePath();
        Artifact artifact = new DefaultArtifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), null, "jar", "main", null);
        artifact.setFile(artifactFile);
        when(project.getGroupId()).thenReturn("group");
        when(project.getArtifactId()).thenReturn("artifact");
        when(project.getVersion()).thenReturn("1.0.0");
        when(project.getArtifact()).thenReturn(artifact);
        when(project.getPackaging()).thenReturn("jar");
        when(project.getParent()).thenReturn(parentProject);
        properties.put(MavenProject.class.getName(), project);

        Build build = new Build();
        build.setOutputDirectory("target/classes");
        build.setTestOutputDirectory("target/test-classes");
        when(project.getBuild()).thenReturn(build);
        MavenProjectDirectoryDescriptor projectDescriptor = mock(MavenProjectDirectoryDescriptor.class);
        List<ArtifactDescriptor> createsArtifacts = new LinkedList<>();
        when(projectDescriptor.getCreatesArtifacts()).thenReturn(createsArtifacts);
        doReturn(projectDescriptor).when(fileResolver)
            .match("/project", MavenProjectDirectoryDescriptor.class, scannerContext);

        Scanner scanner = mock(Scanner.class);
        doReturn(scanConfiguration).when(scanner)
            .getConfiguration();

        // pom.xml
        MavenPomXmlDescriptor pomXmlDescriptor = mock(MavenPomXmlDescriptor.class);
        when(scanner.scan(pomXml, pomXml.getAbsolutePath(), MavenScope.PROJECT)).thenReturn(pomXmlDescriptor);

        // Effective effective model
        MavenPomDescriptor effectiveModelDescriptor = mock(MavenPomDescriptor.class);
        doReturn(effectiveModelDescriptor).when(store)
            .create(MavenPomDescriptor.class);
        Model effectiveModel = mock(Model.class);
        when(project.getModel()).thenReturn(effectiveModel);
        doReturn(effectiveModelDescriptor).when(scanner)
            .scan(any(Model.class), eq(pomXml.getAbsolutePath()), eq(MavenScope.PROJECT));

        // Store and cache
        when(scannerContext.getStore()).thenReturn(store);
        Cache<String, ? extends Descriptor> artifactCache = mock(Cache.class);
        doReturn(artifactCache).when(store)
            .getCache(anyString());
        when(scanner.getContext()).thenReturn(scannerContext);

        // classes directory
        MavenMainArtifactDescriptor mainArtifactDescriptor = mock(MavenMainArtifactDescriptor.class);
        JavaClassesDirectoryDescriptor mainClassesDirectory = mock(JavaClassesDirectoryDescriptor.class);
        MavenTestArtifactDescriptor testArtifactDescriptor = mock(MavenTestArtifactDescriptor.class);
        JavaClassesDirectoryDescriptor testClassesDirectory = mock(JavaClassesDirectoryDescriptor.class);
        doReturn(mainArtifactDescriptor).when(artifactCache)
            .get(argThat(fqn -> fqn.contains(":jar:")), any());
        doReturn(mainClassesDirectory).when(scanner)
            .scan(any(File.class), eq("target/classes"), eq(CLASSPATH));
        doReturn(mainArtifactDescriptor).when(store)
            .addDescriptorType(mainArtifactDescriptor, MavenMainArtifactDescriptor.class);

        // test classes directory
        doReturn(testClassesDirectory).when(scanner)
            .scan(any(File.class), eq("target/test-classes"), eq(CLASSPATH));
        doReturn(testArtifactDescriptor).when(artifactCache)
            .get(argThat(fqn -> fqn.contains(":test-jar:")), any());
        doReturn(testClassesDirectory).when(store)
            .addDescriptorType(testArtifactDescriptor, JavaClassesDirectoryDescriptor.class);
        doReturn(testArtifactDescriptor).when(store)
            .addDescriptorType(testArtifactDescriptor, MavenTestArtifactDescriptor.class);
        doReturn(mainClassesDirectory).when(store)
            .addDescriptorType(mainArtifactDescriptor, JavaClassesDirectoryDescriptor.class);

        doReturn(dependencyGraphBuilder).when(scannerContext)
            .peek(DependencyGraphBuilder.class);
        DependencyNode dependencyNode = mock(DependencyNode.class);
        doReturn(dependencyNode).when(dependencyGraphBuilder)
            .buildDependencyGraph(any(ProjectBuildingRequest.class), eq(null));

        // dependency artifacts

        DependsOnDescriptor testDependsOnMainDescriptor = mock(DependsOnDescriptor.class);
        doReturn(testDependsOnMainDescriptor).when(store)
            .create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor);

        MavenProjectDescriptor parentProjectDescriptor = mock(MavenProjectDescriptor.class);
        doReturn(parentProjectDescriptor).when(store)
            .create(MavenProjectDescriptor.class);

        ProjectBuildingRequest projectBuildingRequest = mock(ProjectBuildingRequest.class);
        doReturn(repositorySystemSession).when(projectBuildingRequest)
            .getRepositorySession();
        doReturn(projectBuildingRequest).when(mavenSession)
            .getProjectBuildingRequest();
        doReturn(localRepo).when(repositorySystemSession)
            .getLocalRepository();
        doReturn(localRepository).when(mavenSession)
            .getLocalRepository();

        doReturn(mavenSession).when(scannerContext)
            .peek(MavenSession.class);

        // scan
        scannerPlugin.configure(scannerContext, properties);
        scannerPlugin.scan(project, null, null, scanner);

        // verify
        verify(scannerContext).push(eq(ArtifactResolver.class), any(MavenRepositoryArtifactResolver.class));
        verify(scannerContext).pop(ArtifactResolver.class);

        verify(scanner).scan(any(File.class), eq("target/classes"), eq(CLASSPATH));
        verify(scanner).scan(any(File.class), eq("target/test-classes"), eq(CLASSPATH));
        verify(fileResolver).match("/project", MavenProjectDirectoryDescriptor.class, scannerContext);
        verify(projectDescriptor).setFullQualifiedName("group:artifact:1.0.0");
        verify(projectDescriptor).setName("project");
        verify(projectDescriptor).setGroupId("group");
        verify(projectDescriptor).setArtifactId("artifact");
        verify(projectDescriptor).setVersion("1.0.0");
        verify(projectDescriptor).setPackaging("jar");
        // Parent
        verify(store).find(MavenProjectDescriptor.class, "group:parent-artifact:1.0.0");
        verify(store).create(MavenProjectDescriptor.class);
        verify(parentProjectDescriptor).setFullQualifiedName("group:parent-artifact:1.0.0");
        verify(projectDescriptor).setParent(parentProjectDescriptor);
        // Model
        verify(scanner).scan(pomXml, pomXml.getAbsolutePath(), MavenScope.PROJECT);
        verify(projectDescriptor).setModel(pomXmlDescriptor);
        // Effective model
        verify(store).create(MavenPomDescriptor.class);
        verify(scannerContext).push(MavenPomDescriptor.class, effectiveModelDescriptor);
        verify(scanner, atLeastOnce()).scan(effectiveModelCaptor.capture(), eq(pomXml.getAbsolutePath()), eq(MavenScope.PROJECT));
        assertThat(effectiveModelCaptor.getValue()
            .getDelegate()).isEqualTo(effectiveModel);
        verify(scannerContext).pop(MavenPomDescriptor.class);
        verify(projectDescriptor).setEffectiveModel(effectiveModelDescriptor);
        verify(artifactCache).get(argThat(fqn -> fqn.contains(":jar:")), any());
        verify(store).addDescriptorType(mainArtifactDescriptor, JavaClassesDirectoryDescriptor.class);
        verify(artifactCache).get(argThat(fqn -> fqn.contains(":test-jar:")), any());
        verify(store).addDescriptorType(testArtifactDescriptor, JavaClassesDirectoryDescriptor.class);

        verify(dependencyGraphBuilder).buildDependencyGraph(any(ProjectBuildingRequest.class), eq(null));
        verify(dependencyScanner).evaluate(eq(dependencyNode), eq(mainArtifactDescriptor), eq(testArtifactDescriptor), eq(expectedDependenciesScan),
            artifactFilterCaptor.capture(), eq(localRepository), eq(scanner));

        verify(store).create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor);

        verify(scannerContext).push(JavaArtifactFileDescriptor.class, mainClassesDirectory);
        verify(scannerContext).push(JavaArtifactFileDescriptor.class, testClassesDirectory);
        verify(scannerContext, times(2)).pop(JavaArtifactFileDescriptor.class);
        assertThat(createsArtifacts.size(), equalTo(2));
        assertThat(createsArtifacts, hasItem(mainArtifactDescriptor));
        assertThat(createsArtifacts, hasItem(testArtifactDescriptor));
    }
}
