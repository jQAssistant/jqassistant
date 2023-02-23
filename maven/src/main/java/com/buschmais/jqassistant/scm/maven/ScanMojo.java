package com.buschmais.jqassistant.scm.maven;

import java.io.File;

import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenRepositoryArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

/**
 * Scans the current Maven project.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = TEST, threadSafe = true)
public class ScanMojo extends AbstractModuleMojo {

    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return true;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void execute(MojoExecutionContext mojoExecutionContext, MavenProject mavenProject) throws MojoExecutionException, MojoFailureException {
        MavenConfiguration configuration = mojoExecutionContext.getConfiguration();
        File outputDirectory = mojoExecutionContext.getOutputDirectory();
        withStore(store -> scan(mavenProject, configuration, store, outputDirectory), mojoExecutionContext);
    }

    private void scan(MavenProject mavenProject, MavenConfiguration configuration, Store store, File outputDirectory) {
        PluginRepository pluginRepository = getPluginRepository(configuration);
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        ScannerContext scannerContext = new ScannerContextImpl(pluginRepository.getClassLoader(), store, outputDirectory);
        Scanner scanner = new ScannerImpl(configuration.scan(), scannerContext, scannerPluginRepository);

        File localRepositoryDirectory = session.getProjectBuildingRequest()
            .getRepositorySession()
            .getLocalRepository()
            .getBasedir();
        FileResolver fileResolver = scannerContext.peek(FileResolver.class);
        MavenRepositoryArtifactResolver repositoryArtifactResolver = new MavenRepositoryArtifactResolver(localRepositoryDirectory, fileResolver);

        scannerContext.push(MavenSession.class, session);
        scannerContext.push(ArtifactResolver.class, repositoryArtifactResolver);
        scannerContext.push(DependencyGraphBuilder.class, dependencyGraphBuilder);
        try {
            scanner.scan(mavenProject, mavenProject.getFile()
                .getAbsolutePath(), MavenScope.PROJECT);
        } finally {
            scannerContext.pop(DependencyGraphBuilder.class);
            scannerContext.pop(ArtifactResolver.class);
            scannerContext.pop(MavenSession.class);
        }
    }
}
