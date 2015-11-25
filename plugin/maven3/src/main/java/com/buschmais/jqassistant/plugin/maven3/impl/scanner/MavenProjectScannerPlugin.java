package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope.TESTREPORTS;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.buschmais.jqassistant.plugin.java.api.scanner.ClasspathScopedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.ScanInclude;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.ArtifactCoordinates;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.MavenArtifactResolver;

/**
 * A scanner plugin for maven projects.
 */
public class MavenProjectScannerPlugin extends AbstractScannerPlugin<MavenProject, MavenProjectDirectoryDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectScannerPlugin.class);

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public MavenProjectDirectoryDescriptor scan(MavenProject project, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        ArtifactResolver artifactResolver = new MavenArtifactResolver();
        context.push(ArtifactResolver.class, artifactResolver);
        try {
            MavenProjectDirectoryDescriptor projectDescriptor = resolveProject(project, MavenProjectDirectoryDescriptor.class, context);
            // resolve dependencies
            Map<ArtifactFileDescriptor, Artifact> mainArtifactDependencies = new HashMap<>();
            Map<ArtifactFileDescriptor, Artifact> testArtifactDependencies = new HashMap<>();
            for (Artifact dependency : project.getDependencyArtifacts()) {
                ArtifactFileDescriptor dependencyDescriptor = getMavenArtifactDescriptor(new ArtifactCoordinates(dependency, false), artifactResolver, scanner);
                if (!Artifact.SCOPE_TEST.equals(dependency.getScope())) {
                    mainArtifactDependencies.put(dependencyDescriptor, dependency);
                }
                testArtifactDependencies.put(dependencyDescriptor, dependency);
            }
            Artifact artifact = project.getArtifact();
            // main artifact
            MavenArtifactDescriptor mainArtifactDescriptor = getMavenArtifactDescriptor(new ArtifactCoordinates(artifact, false), artifactResolver, scanner);
            projectDescriptor.getCreatesArtifacts().add(mainArtifactDescriptor);
            addDependencies(mainArtifactDescriptor, mainArtifactDependencies, scanner.getContext());
            // test artifact
            MavenArtifactDescriptor testArtifactDescriptor = null;
            String testOutputDirectory = project.getBuild().getTestOutputDirectory();
            if (testOutputDirectory != null) {
                testArtifactDescriptor = getMavenArtifactDescriptor(new ArtifactCoordinates(artifact, true), artifactResolver, scanner);
                projectDescriptor.getCreatesArtifacts().add(testArtifactDescriptor);
                testArtifactDependencies.put(mainArtifactDescriptor, artifact);
                addDependencies(testArtifactDescriptor, testArtifactDependencies, scanner.getContext());
            }

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
     * @param scanner
     *            The scanner.
     * @return The artifact descriptor descriptor.
     */
    private MavenArtifactDescriptor getMavenArtifactDescriptor(ArtifactCoordinates artifact, ArtifactResolver artifactResolver, Scanner scanner) {
        return artifactResolver.resolve(artifact, scanner.getContext());
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
        MavenPomXmlDescriptor mavenPomXmlDescriptor = scanner.scan(pomXmlFile, pomXmlFile.getAbsolutePath(), MavenScope.PROJECT);
        projectDescriptor.setModel(mavenPomXmlDescriptor);
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
     * Add dependency relations to the artifacts.
     * 
     * @param artifactDescriptor
     *            The artifact descriptor for adding dependencies to.
     * @param dependencies
     *            The map of dependency artifacts.
     * @param scannerContext
     *            The scanner context.
     */
    private void addDependencies(ArtifactFileDescriptor artifactDescriptor, Map<ArtifactFileDescriptor, Artifact> dependencies, ScannerContext scannerContext) {
        for (Map.Entry<ArtifactFileDescriptor, Artifact> entry : dependencies.entrySet()) {
            ArtifactFileDescriptor dependencyDescriptor = entry.getKey();
            Artifact dependency = entry.getValue();
            DependsOnDescriptor dependsOnDescriptor = scannerContext.getStore().create(artifactDescriptor, DependsOnDescriptor.class, dependencyDescriptor);
            dependsOnDescriptor.setScope(dependency.getScope());
            dependsOnDescriptor.setOptional(dependency.isOptional());
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
            JavaArtifactFileDescriptor javaArtifactFileDescriptor = scanner.getContext().getStore().addDescriptorType(artifactDescriptor,
                    JavaClassesDirectoryDescriptor.class);
            ScannerContext context = scanner.getContext();
            context.push(JavaArtifactFileDescriptor.class, javaArtifactFileDescriptor);
            TypeResolver typeResolver = new ClasspathScopedTypeResolver(javaArtifactFileDescriptor);
            context.push(TypeResolver.class, typeResolver);
            try {
                scanPath(projectDescriptor, directoryName, CLASSPATH, scanner);
            } finally {
                context.pop(TypeResolver.class);
                context.pop(JavaArtifactFileDescriptor.class);
            }
        }
    }

    /**
     * Scan a given path.
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
    private <F extends FileDescriptor> F scanPath(MavenProjectDirectoryDescriptor projectDescriptor, String path, Scope scope, Scanner scanner) {
        File file = new File(path);
        if (file.exists()) {
            return scanPath(projectDescriptor, file, path, scope, scanner);
        }
        LOGGER.debug(file.getAbsolutePath() + " does not exist, skipping.");
        return null;
    }

    /**
     * Scan a given file.
     * <p>
     * The current project is pushed to the context.
     * </p>
     * 
     * @param projectDescriptor
     *            The maven project descriptor.
     * @param directory
     *            The file.
     * @param path
     *            The path.
     * 
     * @param scope
     *            The scope.
     * 
     * @param scanner
     *            The scanner.
     */
    private <F extends FileDescriptor> F scanPath(MavenProjectDirectoryDescriptor projectDescriptor, File directory, String path, Scope scope,
            Scanner scanner) {
        scanner.getContext().push(MavenProjectDirectoryDescriptor.class, projectDescriptor);
        try {
            F fileDescriptor = scanner.scan(directory, path, scope);
            if (fileDescriptor != null) {
                projectDescriptor.getContains().add(fileDescriptor);
            }
            return fileDescriptor;
        } finally {
            scanner.getContext().pop(MavenProjectDirectoryDescriptor.class);
        }
    }
}
