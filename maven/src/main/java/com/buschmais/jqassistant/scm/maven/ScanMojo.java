package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerConfiguration;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenRepositoryArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.ScanInclude;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, threadSafe = true, configurator = "custom")
public class ScanMojo extends AbstractModuleMojo {

    /**
     * Specifies a list of directory names relative to the root module containing
     * additional rule files.
     */
    @Parameter(property = "jqassistant.scan.includes")
    protected List<ScanInclude> scanIncludes;

    /**
     * Specifies properties to be passed to the scanner plugins.
     */
    @Parameter(property = "jqassistant.scan.properties")
    private Map<String, Object> scanProperties;

    /**
     * Specifies if the scanner shall continue if an error is encountered.
     */
    @Parameter(property = "jqassistant.scan.continueOnError")
    private boolean continueOnError = false;

    /**
     * Indicates whether to initially reset the store before scanning.
     */
    @Parameter(property = "jqassistant.store.reset")
    protected boolean reset = true;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return reset;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    /**
     * Return the plugin properties.
     *
     * @return The plugin properties.
     */
    protected Map<String, Object> getPluginProperties() {
        Map<String, Object> properties = new HashMap<>();
        if (scanProperties != null) {
            properties.putAll(scanProperties);
        }
        properties.put(ScanInclude.class.getName(), scanIncludes);
        return properties;
    }

    @Override
    public void execute(MavenProject mavenProject, Store store) throws MojoExecutionException {
        validate();
        ScannerConfiguration configuration = new ScannerConfiguration();
        configuration.setContinueOnError(continueOnError);
        PluginRepository pluginRepository = pluginRepositoryProvider.getPluginRepository();
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        ScannerContext scannerContext = new ScannerContextImpl(store);
        Scanner scanner = new ScannerImpl(configuration, getPluginProperties(), scannerContext, scannerPluginRepository);

        File localRepositoryDirectory = session.getProjectBuildingRequest().getRepositorySession().getLocalRepository().getBasedir();
        FileResolver fileResolver = scannerContext.peek(FileResolver.class);
        MavenRepositoryArtifactResolver repositoryArtifactResolver = new MavenRepositoryArtifactResolver(localRepositoryDirectory, fileResolver);

        scannerContext.push(MavenSession.class, session);
        scannerContext.push(ArtifactResolver.class, repositoryArtifactResolver);
        scannerContext.push(DependencyGraphBuilder.class, dependencyGraphBuilder);
        try {
            scanner.scan(mavenProject, mavenProject.getFile().getAbsolutePath(), MavenScope.PROJECT);
        } finally {
            scannerContext.pop(DependencyGraphBuilder.class);
            scannerContext.pop(ArtifactResolver.class);
            scannerContext.pop(MavenSession.class);
        }
    }

    /**
     * Validate the given configuration.
     *
     * @throws MojoExecutionException
     *             If the validation fails.
     */
    private void validate() throws MojoExecutionException {
        if (scanIncludes != null) {
            for (ScanInclude scanInclude : scanIncludes) {
                if (scanInclude.getPath() != null && scanInclude.getUrl() != null) {
                    throw new MojoExecutionException(
                            "A scanInclude can only include either a file or an URL: path=" + scanInclude.getPath() + ", url=" + scanInclude.getUrl());
                }
            }
        }
    }
}
