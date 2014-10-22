package com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.plugin.junit4.api.scanner.JunitScope.TESTREPORTS;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.AbstractMavenProjectScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.ScanInclude;

/**
 * A project scanner plugin for maven projects.
 */
public class MavenProjectScannerPlugin extends AbstractMavenProjectScannerPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectScannerPlugin.class);

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public MavenProjectDirectoryDescriptor scan(MavenProject project, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        MavenProjectDirectoryDescriptor projectDescriptor = resolveProject(project, MavenProjectDirectoryDescriptor.class, context);
        projectDescriptor.setPackaging(project.getPackaging());
        Artifact artifact = project.getArtifact();
        ArtifactDescriptor mainArtifactDescriptor = scanClassesDirectory(projectDescriptor, artifact, false, project.getBuild().getOutputDirectory(), scanner);
        ArtifactDescriptor testArtifactDescriptor = scanClassesDirectory(projectDescriptor, artifact, true, project.getBuild().getTestOutputDirectory(),
                scanner);
        addProjectDetails(project, projectDescriptor, mainArtifactDescriptor, testArtifactDescriptor, context);
        scanPath(projectDescriptor, project.getBuild().getDirectory() + "/surefire-reports", TESTREPORTS, scanner);
        scanPath(projectDescriptor, project.getBuild().getDirectory() + "/failsafe-reports", TESTREPORTS, scanner);
        List<ScanInclude> scanDirectories = (List<ScanInclude>) getProperties().get(ScanInclude.class.getName());
        if (scanDirectories != null) {
            for (ScanInclude scanInclude : scanDirectories) {
                scanPath(projectDescriptor, scanInclude.getPath(), JavaScope.CLASSPATH, scanner);
            }

        }
        return projectDescriptor;
    }

    /**
     * Add project specific information.
     * 
     * @param project
     *            The project.
     * @param projectDescriptor
     *            The project descriptor.
     * @param mainArtifactDescriptor
     *            The artifact descriptor representing the main artifact.
     * @param testArtifactDescriptor
     *            The artifact descriptor representing the test artifact.
     */
    private void addProjectDetails(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, ArtifactDescriptor mainArtifactDescriptor,
            ArtifactDescriptor testArtifactDescriptor, ScannerContext scannerContext) {
        addParent(project, projectDescriptor, scannerContext);
        addModules(project, projectDescriptor, scannerContext);
        addDependencies(project, mainArtifactDescriptor, testArtifactDescriptor, scannerContext);
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
     */
    private void addModules(MavenProject project, MavenProjectDirectoryDescriptor projectDescriptor, ScannerContext scannerContext) {
        File projectDirectory = project.getBasedir();
        Set<File> modules = new HashSet<>();
        for (String moduleName : (List<String>) project.getModules()) {
            File module = new File(projectDirectory, moduleName);
            modules.add(module);
        }
        for (MavenProject module : (List<MavenProject>) project.getCollectedProjects()) {
            if (modules.contains(module.getBasedir())) {
                MavenProjectDescriptor moduleDescriptor = resolveProject(module, MavenProjectDescriptor.class, scannerContext);
                projectDescriptor.getModules().add(moduleDescriptor);
            }
        }
    }

    /**
     * Add dependency relations to the artifacts.
     * 
     * @param project
     *            The project.
     * @param mainArtifactDescriptor
     *            The artifact descriptor representing the main artifact.
     * @param testArtifactDescriptor
     *            The artifact descriptor representing the test artifact.
     * @param scannerContext
     *            The scanner context.
     */
    private void addDependencies(MavenProject project, ArtifactDescriptor mainArtifactDescriptor, ArtifactDescriptor testArtifactDescriptor,
            ScannerContext scannerContext) {
        if (mainArtifactDescriptor != null && testArtifactDescriptor != null) {
            DependsOnDescriptor dependsOnDescriptor = scannerContext.getStore().create(testArtifactDescriptor, DependsOnDescriptor.class,
                    mainArtifactDescriptor);
            dependsOnDescriptor.setScope(Artifact.SCOPE_TEST);
        }
        for (Artifact dependency : (Set<Artifact>) project.getDependencyArtifacts()) {
            ArtifactDescriptor dependencyDescriptor = resolveArtifact(dependency, scannerContext);
            DependsOnDescriptor dependsOnDescriptor;
            ArtifactDescriptor dependentDescriptor;
            String scope = dependency.getScope();
            if (Artifact.SCOPE_TEST.equals(scope)) {
                dependentDescriptor = testArtifactDescriptor;
            } else {
                dependentDescriptor = mainArtifactDescriptor;
            }
            if (dependentDescriptor != null) {
                dependsOnDescriptor = scannerContext.getStore().create(dependentDescriptor, DependsOnDescriptor.class, dependencyDescriptor);
                dependsOnDescriptor.setScope(scope);
                dependsOnDescriptor.setOptional(dependency.isOptional());
            }
        }
    }

    /**
     * Scan the given directory for classes.
     * 
     * @param directoryName
     *            The directory.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    private ArtifactDescriptor scanClassesDirectory(MavenProjectDirectoryDescriptor projectDescriptor, Artifact artifact, boolean testJar,
            final String directoryName, Scanner scanner) throws IOException {
        ArtifactDescriptor artifactDescriptor = resolveArtifact(artifact, testJar, scanner.getContext());
        projectDescriptor.getCreatesArtifacts().add(artifactDescriptor);
        scanner.getContext().push(ArtifactDescriptor.class, artifactDescriptor);
        try {
            scanPath(projectDescriptor, directoryName, CLASSPATH, scanner);
        } finally {
            scanner.getContext().pop(ArtifactDescriptor.class);
        }
        return artifactDescriptor;
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
    private void scanPath(MavenProjectDirectoryDescriptor projectDescriptor, String path, Scope scope, Scanner scanner) {
        File item = new File(path);
        if (item.exists()) {
            scanner.getContext().push(MavenProjectDirectoryDescriptor.class, projectDescriptor);
            try {
                Descriptor descriptor = scanner.scan(item, path, scope);
                if (descriptor != null) {
                    projectDescriptor.addContains(descriptor);
                }
            } finally {
                scanner.getContext().pop(MavenProjectDirectoryDescriptor.class);
            }
        } else {
            LOGGER.info(path + "' does not exist, skipping scan.");
        }
    }
}
