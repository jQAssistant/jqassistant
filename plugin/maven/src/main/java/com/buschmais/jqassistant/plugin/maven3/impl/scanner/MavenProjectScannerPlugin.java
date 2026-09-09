package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.impl.scanner.PathNormalizer;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.*;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency.DependencyScanner;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.dependency.GraphResolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
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

    private static final String PACKAGING_POM = "pom";

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
    public boolean accepts(MavenProject item, String location, Scope scope) {
        return true;
    }

    @Override
    public MavenProjectDirectoryDescriptor scan(MavenProject project, String location, Scope scope, Scanner scanner) {
        ScannerContext context = scanner.getContext();
        MavenSession mavenSession = context.peek(MavenSession.class);

        File localRepositoryDirectory = mavenSession.getProjectBuildingRequest()
            .getRepositorySession()
            .getLocalRepository()
            .getBasedir();

        FileResolver fileResolver = context.peek(FileResolver.class);
        MavenRepositoryArtifactResolver artifactResolver = new MavenRepositoryArtifactResolver(localRepositoryDirectory, fileResolver, context);

        MavenProjectDirectoryDescriptor projectDescriptor = resolveProject(project, MavenProjectDirectoryDescriptor.class, context);
        context.push(ArtifactResolver.class, artifactResolver);
        try {
            scanArtifacts(project, projectDescriptor, scanner, mavenSession);
            // project information
            addProjectDetails(project, projectDescriptor, scanner);
            scanTestReports(project, scanner);
            scanIncludes(project, scanner, projectDescriptor);
            return projectDescriptor;
        } finally {
            context.pop(ArtifactResolver.class);
        }
    }

    private MavenProjectDirectoryDescriptor scanArtifacts(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, Scanner scanner,
        MavenSession mavenSession) {
        ScannerContext context = scanner.getContext();
        // main artifact
        Artifact artifact = project.getArtifact();
        File mainArtifactFile;
        File outputDirectory = new File(project.getBuild()
            .getOutputDirectory());
        if (project.getPackaging()
            .equals(PACKAGING_POM)) {
            mainArtifactFile = project.getModel()
                .getPomFile();
        } else {
            mainArtifactFile = outputDirectory;
        }
        MavenMainArtifactDescriptor mainArtifactDescriptor = getMavenArtifactDescriptor(mainArtifactFile, new MavenArtifactCoordinates(artifact, false),
            MavenMainArtifactDescriptor.class, context);
        projectDescriptor.getCreatesArtifacts()
            .add(mainArtifactDescriptor);

        // test artifact
        File testOutputDirectory = new File(project.getBuild()
            .getTestOutputDirectory());
        MavenArtifactDescriptor testArtifactDescriptor = getMavenArtifactDescriptor(testOutputDirectory, new MavenArtifactCoordinates(artifact, true),
            MavenTestArtifactDescriptor.class, context);
        DependsOnDescriptor dependsOnDescriptor = context.getStore()
            .create(testArtifactDescriptor, DependsOnDescriptor.class, mainArtifactDescriptor);
        dependsOnDescriptor.setScope(Artifact.SCOPE_COMPILE);
        projectDescriptor.getCreatesArtifacts()
            .add(testArtifactDescriptor);

        resolveDependencyGraph(project, mainArtifactDescriptor, testArtifactDescriptor, scanner, mavenSession);

        if (!project.getPackaging()
            .equals(PACKAGING_POM)) {
            // Scan classes
            scanClassesDirectory(mainArtifactDescriptor, outputDirectory, scanner);
            scanClassesDirectory(testArtifactDescriptor, testOutputDirectory, scanner);
        }
        return projectDescriptor;
    }

    private void scanTestReports(MavenProject project, Scanner scanner) {
        // add test reports
        String surefireReports = project.getBuild()
            .getDirectory() + "/surefire-reports";
        scanFile(new File(surefireReports), TESTREPORTS, scanner);
        String failsafeReports = project.getBuild()
            .getDirectory() + "/failsafe-reports";
        scanFile(new File(failsafeReports), TESTREPORTS, scanner);
    }

    private void scanIncludes(MavenProject project, Scanner scanner, MavenProjectDirectoryDescriptor projectDescriptor) {
        File basedir = project.getBasedir();
        Consumer<Descriptor> scanIncludeConsumer = descriptor -> {
            if (descriptor instanceof FileDescriptor) {
                projectDescriptor.getContains()
                    .add((FileDescriptor) descriptor);
            }
        };

        // add additional includes
        scanner.getConfiguration()
            .include()
            .ifPresent(include -> {
                // files
                scanInclude(include.files(), (fileName, s) -> scanFile(basedir.toPath()
                    .resolve(fileName)
                    .toFile(), s, scanner), scanIncludeConsumer, scanner);
                // urls
                scanInclude(include.urls(), (url, s) -> {
                    try {
                        // scan URL as URI to allow more flexibility on protocols
                        return scanner.scan(new URI(url), url, s);
                    } catch (URISyntaxException e) {
                        LOGGER.warn("Cannot convert URL '" + url + "' to URI.", e);
                        return null;
                    }
                }, scanIncludeConsumer, scanner);
            });
    }

    private void scanInclude(Optional<List<String>> resources, BiFunction<String, Scope, Descriptor> scanAction, Consumer<Descriptor> descriptorConsumer,
        Scanner scanner) {
        resources.ifPresent(r -> {
            for (ScopeHelper.ScopedResource scopedResource : scopeHelper.getScopedResources(r)) {
                String resource = scopedResource.getResource();
                String scopeName = scopedResource.getScopeName();
                Scope resolvedScope = scanner.resolveScope(scopeName);
                Descriptor descriptor = scanAction.apply(resource, resolvedScope);
                if (descriptor != null) {
                    descriptorConsumer.accept(descriptor);
                }
            }
        });
    }

    /**
     * Returns a resolved maven artifact descriptor for the given coordinates.
     *
     * @param file
     *     The file that represents a Maven artifact.
     * @param coordinates
     *     The artifact coordinates.
     * @param type
     *     The expected type.
     * @param context
     *     The {@link ScannerContext}.
     * @return The artifact descriptor.
     */
    private <T extends MavenArtifactFileDescriptor> T getMavenArtifactDescriptor(File file, Coordinates coordinates, Class<T> type, ScannerContext context) {
        String path = PathNormalizer.normalizeFileName(file, context);
        T artifactFileDescriptor = context.peek(FileResolver.class)
            .require(path, type, context);
        MavenArtifactHelper.setCoordinates(artifactFileDescriptor, coordinates);
        return artifactFileDescriptor;
    }

    /**
     * Resolves a maven project.
     *
     * @param project
     *     The project
     * @param scannerContext
     *     The scanner context.
     * @return The maven project descriptor.
     */
    protected <T extends MavenProjectDescriptor> T resolveProject(MavenProject project, Class<T> expectedType, ScannerContext scannerContext) {
        String id = String.format("%s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion());
        Store store = scannerContext.getStore();
        MavenProjectDescriptor projectDescriptor = store.find(MavenProjectDescriptor.class, id);
        if (projectDescriptor == null) {
            // resolve project as directory if a basedir is present (local project)
            File basedir = project.getBasedir();
            if (basedir != null) {
                projectDescriptor = scannerContext.peek(FileResolver.class)
                    .match(PathNormalizer.normalizeFileName(basedir, scannerContext), MavenProjectDirectoryDescriptor.class, scannerContext);

            } else {
                projectDescriptor = store.create(expectedType);
            }
            projectDescriptor.setFullQualifiedName(id);
            projectDescriptor.setName(project.getName());
            projectDescriptor.setGroupId(project.getGroupId());
            projectDescriptor.setArtifactId(project.getArtifactId());
            projectDescriptor.setVersion(project.getVersion());
            projectDescriptor.setPackaging(project.getPackaging());
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
        FileDescriptor mavenPomXmlDescriptor = scanner.scan(pomXmlFile, null, MavenScope.PROJECT);
        projectDescriptor.setModel(mavenPomXmlDescriptor);
        // Effective model
        MavenPomDescriptor mavenPomDescriptor = scanner.getContext()
            .getStore()
            .create(MavenPomDescriptor.class);
        scanner.getContext()
            .push(MavenPomDescriptor.class, mavenPomDescriptor);
        scanner.scan(project.getModel(), null, MavenScope.PROJECT);
        scanner.getContext()
            .pop(MavenPomDescriptor.class);
        MavenPomDescriptor effectiveModelDescriptor = scanner.getContext()
            .getStore()
            .addDescriptorType(mavenPomDescriptor, EffectiveDescriptor.class, MavenPomDescriptor.class);
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
                MavenProjectDirectoryDescriptor moduleDescriptor = resolveProject(module, MavenProjectDirectoryDescriptor.class, scannerContext);
                projectDescriptor.getModules()
                    .add(moduleDescriptor);
            }
        }
    }

    /**
     * Scan the given directory for classes and add them to an artifact.
     *
     * @param artifactDescriptor
     *     The artifact.
     * @param directory
     *     The directory.
     * @param scanner
     *     The scanner.
     */
    private void scanClassesDirectory(MavenArtifactDescriptor artifactDescriptor, final File directory, Scanner scanner) {
        if (directory.exists()) {
            scanArtifact(artifactDescriptor, directory, scanner);
        }
    }

    /**
     * Scan a {@link File} that represents a Java artifact.
     *
     * @param artifactDescriptor
     *     The resolved {@link MavenArtifactDescriptor}.
     * @param file
     *     The {@link File}.
     * @param scanner
     *     The {@link Scanner}.
     */
    private void scanArtifact(ArtifactDescriptor artifactDescriptor, File file, Scanner scanner) {
        JavaArtifactFileDescriptor javaArtifactFileDescriptor = scanner.getContext()
            .getStore()
            .addDescriptorType(artifactDescriptor, JavaArtifactFileDescriptor.class);
        ScannerContext context = scanner.getContext();
        context.push(JavaArtifactFileDescriptor.class, javaArtifactFileDescriptor);
        try {
            scanFile(file, CLASSPATH, scanner);
        } finally {
            context.pop(JavaArtifactFileDescriptor.class);
        }
    }

    /**
     * Scan a given file.
     *
     * <p>
     * The current project is pushed to the context.
     * </p>
     *
     * @param file
     *     The file.
     * @param scope
     *     The scope.
     * @param scanner
     *     The scanner.
     */
    private <F extends FileDescriptor> F scanFile(File file, Scope scope, Scanner scanner) {
        if (file.exists()) {
            return scanner.scan(file, null, scope);
        } else {
            LOGGER.debug("{} does not exist, skipping.", file.getAbsolutePath());
        }
        return null;
    }
}
