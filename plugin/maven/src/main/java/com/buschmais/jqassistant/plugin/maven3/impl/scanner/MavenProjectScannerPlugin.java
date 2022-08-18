package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactFilter;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.EffectiveModel;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency.DependencyScanner;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency.GraphResolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope.TESTREPORTS;
import static org.eclipse.aether.util.graph.transformer.ConflictResolver.CONFIG_PROP_VERBOSE;

/**
 * A scanner plugin for maven projects.
 */
public class MavenProjectScannerPlugin extends AbstractScannerPlugin<MavenProject, MavenProjectDirectoryDescriptor> {

    private static final String PROPERTY_NAME_DEPENDENCIES_SCAN = "maven3.dependencies.scan";

    private static final String PROPERTY_NAME_DEPENDENCIES_INCLUDES = "maven3.dependencies.includes";

    private static final String PROPERTY_NAME_DEPENDENCIES_EXCLUDES = "maven3.dependencies.excludes";

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectScannerPlugin.class);

    private final ScopeHelper scopeHelper = new ScopeHelper(LOGGER);

    private final DependencyScanner dependencyScanner;

    private boolean scanDependencies;

    private ArtifactFilter dependencyFilter = null;

    /**
     * Default constructor.
     */
    public MavenProjectScannerPlugin() {
        this(new DependencyScanner(new GraphResolver()));
    }

    /**
     * Constructor.
     *
     * @param dependencyScanner
     *     The {@link DependencyScanner} to use.
     */
    MavenProjectScannerPlugin(DependencyScanner dependencyScanner) {
        this.dependencyScanner = dependencyScanner;
    }

    @Override
    protected void configure() {
        scanDependencies = getBooleanProperty(PROPERTY_NAME_DEPENDENCIES_SCAN, false);
        String dependencyFilterIncludes = getStringProperty(PROPERTY_NAME_DEPENDENCIES_INCLUDES, null);
        String dependencyFilterExcludes = getStringProperty(PROPERTY_NAME_DEPENDENCIES_EXCLUDES, null);
        dependencyFilter = new ArtifactFilter(dependencyFilterIncludes, dependencyFilterExcludes);

    }

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) {
        return true;
    }

    @Override
    public MavenProjectDirectoryDescriptor scan(MavenProject project, String projectPath, Scope scope, Scanner scanner) {
        ScannerContext context = scanner.getContext();
        MavenSession mavenSession = context.peek(MavenSession.class);
        MavenProjectDirectoryDescriptor projectDescriptor = resolveProject(project, MavenProjectDirectoryDescriptor.class, context);
        ArtifactResolver artifactResolver = context.peek(ArtifactResolver.class);
        // main artifact
        Artifact artifact = project.getArtifact();
        MavenMainArtifactDescriptor mainArtifactDescriptor = getMavenArtifactDescriptor(new MavenArtifactCoordinates(artifact, false),
            MavenMainArtifactDescriptor.class, artifactResolver, scanner);
        projectDescriptor.getCreatesArtifacts()
            .add(mainArtifactDescriptor);
        // test artifact
        MavenArtifactDescriptor testArtifactDescriptor = null;
        String testOutputDirectory = project.getBuild()
            .getTestOutputDirectory();
        if (testOutputDirectory != null) {
            testArtifactDescriptor = getMavenArtifactDescriptor(new MavenArtifactCoordinates(artifact, true), MavenTestArtifactDescriptor.class,
                artifactResolver, scanner);
            DependsOnDescriptor dependsOnDescriptor = context.getStore()
                .create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor);
            dependsOnDescriptor.setScope(Artifact.SCOPE_COMPILE);
            projectDescriptor.getCreatesArtifacts()
                .add(testArtifactDescriptor);
        }
        resolveDependencyGraph(project, mainArtifactDescriptor, testArtifactDescriptor, scanner, mavenSession);

        // Scan classes
        scanClassesDirectory(projectDescriptor, mainArtifactDescriptor, project.getBuild()
            .getOutputDirectory(), scanner);
        if (testOutputDirectory != null) {
            scanClassesDirectory(projectDescriptor, testArtifactDescriptor, testOutputDirectory, scanner);
        }

        // project information
        addProjectDetails(project, projectDescriptor, scanner);
        // add test reports
        scanPath(projectDescriptor, project.getBuild()
            .getDirectory() + "/surefire-reports", TESTREPORTS, scanner);
        scanPath(projectDescriptor, project.getBuild()
            .getDirectory() + "/failsafe-reports", TESTREPORTS, scanner);

        File basedir = project.getBasedir();

        // add additional includes
        scanner.getConfiguration()
            .include()
            .ifPresent(include -> {
                // files
                scanInclude(include.files(), (fileName, s) -> scanFile(projectDescriptor, basedir.toPath()
                    .resolve(fileName)
                    .toFile(), fileName, s, scanner), projectDescriptor, scanner);
                // urls
                scanInclude(include.urls(), (url, s) -> {
                    try {
                        // scan URL as URI to allow more flexibility on protocols
                        return scanner.scan(new URI(url), url, s);
                    } catch (URISyntaxException e) {
                        LOGGER.warn("Cannot convert URL '" + url + "' to URI.", e);
                        return null;
                    }
                }, projectDescriptor, scanner);
            });
        return projectDescriptor;
    }

    private void scanInclude(Optional<List<String>> resources, BiFunction<String, Scope, Descriptor> scanAction,
        MavenProjectDirectoryDescriptor projectDescriptor, Scanner scanner) {
        resources.ifPresent(r -> {
            for (ScopeHelper.ScopedResource scopedResource : scopeHelper.getScopedResources(r)) {
                String resource = scopedResource.getResource();
                String scopeName = scopedResource.getScopeName();
                Scope resolvedScope = scanner.resolveScope(scopeName);
                Descriptor descriptor = scanAction.apply(resource, resolvedScope);
                if (descriptor != null && descriptor instanceof FileDescriptor) {
                    projectDescriptor.getContains()
                        .add((FileDescriptor) descriptor);
                }
            }
        });
    }

    /**
     * Returns a resolved maven artifact descriptor for the given coordinates.
     *
     * @param coordinates
     *     The artifact coordinates.
     * @param type
     *     The expected type.
     * @param artifactResolver
     *     The {@link ArtifactResolver}.
     * @param scanner
     *     The scanner.
     * @return The artifact descriptor.
     */
    private <T extends MavenArtifactDescriptor> T getMavenArtifactDescriptor(Coordinates coordinates, Class<T> type, ArtifactResolver artifactResolver,
        Scanner scanner) {
        MavenArtifactDescriptor mavenArtifactDescriptor = artifactResolver.resolve(coordinates, scanner.getContext());
        return scanner.getContext()
            .getStore()
            .addDescriptorType(mavenArtifactDescriptor, type);
    }

    /**
     * Resolves a maven project.
     *
     * @param project
     *     The project
     * @param expectedType
     *     The expected descriptor type.
     * @param scannerContext
     *     The scanner context.
     * @param <T>
     *     The expected descriptor type.
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
            projectDescriptor = store.addDescriptorType(projectDescriptor, expectedType);
        }
        return expectedType.cast(projectDescriptor);
    }

    private void resolveDependencyGraph(MavenProject project, MavenArtifactDescriptor mainDescriptor, MavenArtifactDescriptor testDescriptor, Scanner scanner,
        MavenSession mavenSession) {
        ScannerContext context = scanner.getContext();
        ProjectBuildingRequest projectBuildingRequest = mavenSession.getProjectBuildingRequest();
        ArtifactRepository localRepository = mavenSession.getLocalRepository();
        DependencyGraphBuilder dependencyGraphBuilder = context.peek(DependencyGraphBuilder.class);
        RepositorySystemSession repositorySession = projectBuildingRequest.getRepositorySession();
        DefaultRepositorySystemSession repositorySystemSession = getVerboseRepositorySystemSession(repositorySession);
        ProjectBuildingRequest buildingRequest = getProjectBuildingRequest(project, projectBuildingRequest, repositorySystemSession);
        DependencyNode rootNode = null;
        try {
            rootNode = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, null);
        } catch (DependencyGraphBuilderException e) {
            LOGGER.warn("Cannot resolve dependency graph for " + project, e);
        }
        if (rootNode != null) {
            dependencyScanner.evaluate(rootNode, mainDescriptor, testDescriptor, scanDependencies, dependencyFilter, localRepository, scanner);
        }
    }

    private DefaultRepositorySystemSession getVerboseRepositorySystemSession(RepositorySystemSession repositorySession) {
        DefaultRepositorySystemSession repositorySystemSession = new DefaultRepositorySystemSession(repositorySession);
        repositorySystemSession.setConfigProperty(CONFIG_PROP_VERBOSE, "true");
        return repositorySystemSession;
    }

    private ProjectBuildingRequest getProjectBuildingRequest(MavenProject project, ProjectBuildingRequest projectBuildingRequest,
        DefaultRepositorySystemSession repositorySystemSession) {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(projectBuildingRequest);
        buildingRequest.setRepositorySession(repositorySystemSession);
        buildingRequest.setProject(project);
        return buildingRequest;
    }

    /**
     * Add project specific information.
     *
     * @param project
     *     The project.
     * @param projectDescriptor
     *     The project descriptor.
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
     *     The Maven project
     * @param projectDescriptor
     *     The project descriptor.
     * @param scanner
     *     The scanner.
     */
    private void addModel(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, Scanner scanner) {
        File pomXmlFile = project.getFile();
        FileDescriptor mavenPomXmlDescriptor = scanner.scan(pomXmlFile, pomXmlFile.getAbsolutePath(), MavenScope.PROJECT);
        projectDescriptor.setModel(mavenPomXmlDescriptor);
        // Effective model
        MavenPomDescriptor effectiveModelDescriptor = scanner.getContext()
            .getStore()
            .create(MavenPomDescriptor.class);
        Model model = new EffectiveModel(project.getModel());
        scanner.getContext()
            .push(MavenPomDescriptor.class, effectiveModelDescriptor);
        scanner.scan(model, pomXmlFile.getAbsolutePath(), MavenScope.PROJECT);
        scanner.getContext()
            .pop(MavenPomDescriptor.class);
        projectDescriptor.setEffectiveModel(effectiveModelDescriptor);
    }

    /**
     * Add the relation to the parent project.
     *
     * @param project
     *     The project.
     * @param projectDescriptor
     *     The project descriptor.
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
     *     The project.
     * @param projectDescriptor
     *     The project descriptor.
     * @param scannerContext
     *     The scanner context.
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
                projectDescriptor.getModules()
                    .add(moduleDescriptor);
            }
        }
    }

    /**
     * Scan the given directory for classes and add them to an artifact.
     *
     * @param projectDescriptor
     *     The maven project.
     * @param artifactDescriptor
     *     The artifact.
     * @param directoryName
     *     The name of the directory.
     * @param scanner
     *     The scanner.
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
     *     The maven project descriptor.
     * @param artifactDescriptor
     *     The resolved {@link MavenArtifactDescriptor}.
     * @param file
     *     The {@link File}.
     * @param path
     *     The path of the file.
     * @param scanner
     *     The {@link Scanner}.
     */
    private JavaArtifactFileDescriptor scanArtifact(MavenProjectDirectoryDescriptor projectDescriptor, ArtifactDescriptor artifactDescriptor, File file,
        String path, Scanner scanner) {
        JavaArtifactFileDescriptor javaArtifactFileDescriptor = scanner.getContext()
            .getStore()
            .addDescriptorType(artifactDescriptor, JavaClassesDirectoryDescriptor.class);
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
     *     The maven project descriptor.
     * @param path
     *     The path.
     * @param scope
     *     The scope.
     * @param scanner
     *     The scanner.
     */
    private <F extends FileDescriptor> F scanPath(MavenProjectDirectoryDescriptor projectDescriptor, String path, Scope scope, Scanner scanner) {
        File file = new File(path);
        return scanFile(projectDescriptor, file, path, scope, scanner);
    }

    /**
     * Scan a given file.
     *
     * <p>
     * The current project is pushed to the context.
     * </p>
     *
     * @param projectDescriptor
     *     The maven project descriptor.
     * @param file
     *     The file.
     * @param path
     *     The path.
     * @param scope
     *     The scope.
     * @param scanner
     *     The scanner.
     */
    private <F extends FileDescriptor> F scanFile(MavenProjectDirectoryDescriptor projectDescriptor, File file, String path, Scope scope, Scanner scanner) {
        if (file.exists()) {
            scanner.getContext()
                .push(MavenProjectDirectoryDescriptor.class, projectDescriptor);
            try {
                return scanner.scan(file, path, scope);
            } finally {
                scanner.getContext()
                    .pop(MavenProjectDirectoryDescriptor.class);
            }
        } else {
            LOGGER.debug(file.getAbsolutePath() + " does not exist, skipping.");
        }
        return null;
    }
}
