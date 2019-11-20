package com.buschmais.jqassistant.scm.maven;

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
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.ScanInclude;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
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
        ScannerConfiguration configuration = new ScannerConfiguration();
        configuration.setContinueOnError(continueOnError);
        ScannerContext scannerContext = new ScannerContextImpl(store);
        scannerContext.push(MavenSession.class, session);
        scannerContext.push(DependencyGraphBuilder.class, dependencyGraphBuilder);
        PluginRepository pluginRepository = pluginRepositoryProvider.getPluginRepository();
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        Scanner scanner = new ScannerImpl(configuration, getPluginProperties(), scannerContext, scannerPluginRepository);
        scanner.scan(mavenProject, mavenProject.getFile().getAbsolutePath(), MavenScope.PROJECT);
        scannerContext.pop(DependencyGraphBuilder.class);
        scannerContext.pop(MavenSession.class);
    }
}
