package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.ScanInclude;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.ArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.MavenArtifactResolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope.TESTREPORTS;

/**
 * A scanner plugin for maven projects.
 */
public class MavenProjectScannerPlugin extends AbstractScannerPlugin<MavenProject, MavenProjectDirectoryDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectScannerPlugin.class);

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) {
        return true;
    }

    @Override
    public MavenProjectDirectoryDescriptor scan(MavenProject project, String path, Scope scope, Scanner scanner) {
        ScannerContext context = scanner.getContext();
        ArtifactResolver artifactResolver = new MavenArtifactResolver();
        context.push(ArtifactResolver.class, artifactResolver);
        try {
            MavenProjectDirectoryDescriptor projectDescriptor = resolveProject(project, MavenProjectDirectoryDescriptor.class, context);
            // resolve dependencies
            Artifact artifact = project.getArtifact();
            // main artifact
            MavenMainArtifactDescriptor mainArtifactDescriptor = getMavenArtifactDescriptor(new ArtifactCoordinates(artifact, false), artifactResolver,
                    MavenMainArtifactDescriptor.class, scanner);
            projectDescriptor.getCreatesArtifacts().add(mainArtifactDescriptor);
            // test artifact
            MavenArtifactDescriptor testArtifactDescriptor = null;
            String testOutputDirectory = project.getBuild().getTestOutputDirectory();
            if (testOutputDirectory != null) {
                testArtifactDescriptor = getMavenArtifactDescriptor(new ArtifactCoordinates(artifact, true), artifactResolver,
                        MavenTestArtifactDescriptor.class, scanner);
                projectDescriptor.getCreatesArtifacts().add(testArtifactDescriptor);
                DependsOnDescriptor dependsOnDescriptor = context.getStore().create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor);
                dependsOnDescriptor.setScope(Artifact.SCOPE_COMPILE);
            }
            resolveDependencyGraph(project, mainArtifactDescriptor, testArtifactDescriptor, artifactResolver, context);

            // Scan classes
            scanClassesDirectory(projectDescriptor, mainArtifactDescriptor, project.getBuild().getOutputDirectory(), scanner);
            if (testOutputDirectory != null) {
                scanClassesDirectory(projectDescriptor, testArtifactDescriptor, testOutputDirectory, scanner);
            }

            // project information
            addProjectDetails(project, projectDescriptor, scanner);
            // add test reports
            scanPath(projectDescriptor, project.getBuild().getDirectory() + "/surefire-reports", TESTREPORTS, scanner);
            scanPath(projectDescriptor, project.getBuild().getDirectory() + "/failsafe-reports", TESTREPORTS, scanner);
            // add additional includes
            List<ScanInclude> scanIncludes = getProperty(ScanInclude.class.getName(), List.class);
            if (scanIncludes != null) {
                for (ScanInclude scanInclude : scanIncludes) {
                    String scopeName = scanInclude.getScope();
                    Scope includeScope = scanner.resolveScope(scopeName);
                    scanPath(projectDescriptor, scanInclude.getPath(), includeScope, scanner);
                }
            }
            return projectDescriptor;
        } finally {
            context.pop(ArtifactResolver.class);
        }
    }

    /**
     * Returns a resolved maven artifact descriptor for the given coordinates.
     * 
     * @param artifact
     *            The artifact.
     * @param artifactResolver
     *            The artifact resolver.
     * @param type
     *            The expected type.
     * @param scanner
     *            The scanner.
     * @return The artifact descriptor.
     */
    private <T extends MavenArtifactDescriptor> T getMavenArtifactDescriptor(ArtifactCoordinates artifact, ArtifactResolver artifactResolver, Class<T> type,
            Scanner scanner) {
        MavenArtifactDescriptor mavenArtifactDescriptor = artifactResolver.resolve(artifact, scanner.getContext());
        return scanner.getContext().getStore().addDescriptorType(mavenArtifactDescriptor, type);
    }

    /**
     * Resolves a maven project.
     * 
     * @param project
     *            The project
     * @param expectedType
     *            The expected descriptor type.
     * @param scannerContext
     *            The scanner context.
     * @param <T>
     *            The expected descriptor type.
     * @return The maven project descriptor.
     */
    protected <T extends MavenProjectDescriptor> T resolveProject(MavenProject project, Class<T> expectedType, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        String id = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        MavenProjectDescriptor projectDescriptor = store.find(MavenProjectDescriptor.class, id);
        if (projectDescriptor == null) {
            projectDescriptor = store.create(expectedType, id);
            projectDescriptor.setName(project.getName());
            projectDescriptor.setGroupId(project.getGroupId());
            projectDescriptor.setArtifactId(project.getArtifactId());
            projectDescriptor.setVersion(project.getVersion());
            projectDescriptor.setPackaging(project.getPackaging());
            projectDescriptor.setFullQualifiedName(id);
        } else if (!expectedType.isAssignableFrom(projectDescriptor.getClass())) {
            projectDescriptor = store.migrate(projectDescriptor, expectedType);
        }
        return expectedType.cast(projectDescriptor);
    }

    private void resolveDependencyGraph(MavenProject project, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor,
            ArtifactResolver artifactResolver, ScannerContext context) {
        MavenSession session = context.peek(MavenSession.class);
        DependencyGraphBuilder dependencyGraphBuilder = context.peek(DependencyGraphBuilder.class);
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setProject(project);
        DependencyNode rootNode = null;
        try {
            rootNode = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, null);
        } catch (DependencyGraphBuilderException e) {
            LOGGER.warn("Cannot resolve dependency graph for " + project, e);
        }
        if (rootNode != null) {
            DependencyNodeVisitor visitor = new DependencyTreeResolver(project, mainDescriptor, testDescriptor, artifactResolver, context);
            rootNode.accept(visitor);
        }
    }

    /**
     * Add project specific information.
     * 
     * @param project
     *            The project.
     * @param projectDescriptor
     *            The project descriptor.
     */
    private void addProjectDetails(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, Scanner scanner) {
        ScannerContext scannerContext = scanner.getContext();
        addParent(project, projectDescriptor, scannerContext);
        addModules(project, projectDescriptor, scannerContext);
        addModel(project, projectDescriptor, scanner);
    }

    /**
     * Scan the pom.xml file and add it as model.
     * 
     * @param project
     *            The Maven project
     * @param projectDescriptor
     *            The project descriptor.
     * @param scanner
     *            The scanner.
     */
    private void addModel(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, Scanner scanner) {
        File pomXmlFile = project.getFile();
        FileDescriptor mavenPomXmlDescriptor = scanner.scan(pomXmlFile, pomXmlFile.getAbsolutePath(), MavenScope.PROJECT);
        projectDescriptor.setModel(mavenPomXmlDescriptor);
        // Effective model
        MavenPomDescriptor effectiveModelDescriptor = scanner.getContext().getStore().create(MavenPomDescriptor.class);
        Model model = project.getModel();
        scanner.getContext().push(MavenPomDescriptor.class, effectiveModelDescriptor);
        scanner.scan(model, pomXmlFile.getAbsolutePath(), MavenScope.PROJECT);
        scanner.getContext().pop(MavenPomDescriptor.class);
        projectDescriptor.setEffectiveModel(effectiveModelDescriptor);
    }

    /**
     * Add the relation to the parent project.
     * 
     * @param project
     *            The project.
     * @param projectDescriptor
     *            The project descriptor.
     */
    private void addParent(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, ScannerContext scannerContext) {
        MavenProject parent = project.getParent();
        if (parent != null) {
            MavenProjectDescriptor parentDescriptor = resolveProject(parent, MavenProjectDescriptor.class, scannerContext);
            projectDescriptor.setParent(parentDescriptor);
        }
    }

    /**
     * Add relations to the modules.
     * 
     * @param project
     *            The project.
     * @param projectDescriptor
     *            The project descriptor.
     * @param scannerContext
     *            The scanner context.
     */
    private void addModules(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, ScannerContext scannerContext) {
        File projectDirectory = project.getBasedir();
        Set<File> modules = new HashSet<>();
        for (String moduleName : project.getModules()) {
            File module = new File(projectDirectory, moduleName);
            modules.add(module);
        }
        for (MavenProject module : project.getCollectedProjects()) {
            if (modules.contains(module.getBasedir())) {
                MavenProjectDescriptor moduleDescriptor = resolveProject(module, MavenProjectDescriptor.class, scannerContext);
                projectDescriptor.getModules().add(moduleDescriptor);
            }
        }
    }

    /**
     * Scan the given directory for classes and add them to an artifact.
     * 
     * @param projectDescriptor
     *            The maven project.
     * @param artifactDescriptor
     *            The artifact.
     * @param directoryName
     *            The name of the directory.
     * @param scanner
     *            The scanner.
     */
    private void scanClassesDirectory(MavenProjectDirectoryDescriptor projectDescriptor, MavenArtifactDescriptor artifactDescriptor, final String directoryName,
            Scanner scanner) {
        File directory = new File(directoryName);
        if (directory.exists()) {
            scanArtifact(projectDescriptor, artifactDescriptor, directory, directoryName, scanner);
        }
    }

    /**
     * Scan a {@link File} that represents a Java artifact.
     * 
     * @param projectDescriptor
     *            The maven project descriptor.
     * @param artifactDescriptor
     *            The resolved {@link MavenArtifactDescriptor}.
     * @param file
     *            The {@link File}.
     * @param path
     *            The path of the file.
     * @param scanner
     *            The {@link Scanner}.
     */
    private JavaArtifactFileDescriptor scanArtifact(MavenProjectDirectoryDescriptor projectDescriptor, ArtifactFileDescriptor artifactDescriptor, File file,
            String path, Scanner scanner) {
        JavaArtifactFileDescriptor javaArtifactFileDescriptor = scanner.getContext().getStore().addDescriptorType(artifactDescriptor,
                JavaClassesDirectoryDescriptor.class);
        ScannerContext context = scanner.getContext();
        context.push(JavaArtifactFileDescriptor.class, javaArtifactFileDescriptor);
        try {
            return scanFile(projectDescriptor, file, path, CLASSPATH, scanner);
        } finally {
            context.pop(JavaArtifactFileDescriptor.class);
        }
    }

    /**
     * Scan a given path and add it to
     * {@link MavenProjectDirectoryDescriptor#getContains()}.
     *
     * @param projectDescriptor
     *            The maven project descriptor.
     * @param path
     *            The path.
     * @param scope
     *            The scope.
     * @param scanner
     *            The scanner.
     */
    private <F extends FileDescriptor> void scanPath(MavenProjectDirectoryDescriptor projectDescriptor, String path, Scope scope, Scanner scanner) {
        File file = new File(path);
        if (!file.exists()) {
            LOGGER.debug(file.getAbsolutePath() + " does not exist, skipping.");
        } else {
            F fileDescriptor = scanFile(projectDescriptor, file, path, scope, scanner);
            if (fileDescriptor != null) {
                projectDescriptor.getContains().add(fileDescriptor);
            }
        }
    }

    /**
     * Scan a given file.
     * 
     * <p>
     * The current project is pushed to the context.
     * </p>
     * 
     * @param projectDescriptor
     *            The maven project descriptor.
     * @param file
     *            The file.
     * @param path
     *            The path.
     * @param scope
     *            The scope.
     * @param scanner
     *            The scanner.
     */
    private <F extends FileDescriptor> F scanFile(MavenProjectDirectoryDescriptor projectDescriptor, File file, String path, Scope scope, Scanner scanner) {
        scanner.getContext().push(MavenProjectDirectoryDescriptor.class, projectDescriptor);
        try {
            return scanner.scan(file, path, scope);
        } finally {
            scanner.getContext().pop(MavenProjectDirectoryDescriptor.class);
        }
    }
}
