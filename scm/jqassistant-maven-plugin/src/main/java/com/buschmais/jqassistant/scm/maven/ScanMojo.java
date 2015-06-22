package com.buschmais.jqassistant.scm.maven;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScopePluginRepository;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.ScanInclude;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, threadSafe = true)
public class ScanMojo extends AbstractModuleMojo {

    /**
     * Specifies a list of directory names relative to the root module
     * containing additional rule files.
     */
    @Parameter(property = "jqassistant.scan.includes")
    protected List<ScanInclude> scanIncludes;

    /**
     * Specifies properties to be passed to the scanner plugins.
     */
    @Parameter(property = "jqassistant.scan.properties")
    private Map<String, Object> scanProperties;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return true;
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
    public void execute(MavenProject mavenProject, Store store) throws MojoExecutionException, MojoFailureException {
        List<ScannerPlugin<?, ?>> scannerPlugins;
        ScannerPluginRepository scannerPluginRepository = pluginRepositoryProvider.getScannerPluginRepository();
        try {
            scannerPlugins = scannerPluginRepository.getScannerPlugins(getPluginProperties());
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot determine scanner plugins.", e);
        }
        ScopePluginRepository scopePluginRepository = pluginRepositoryProvider.getScopePluginRepository();
        ScannerContext scannerContext = new ScannerContextImpl(store);
        Scanner scanner = new ScannerImpl(scannerContext, scannerPlugins, scopePluginRepository.getScopes());
        store.beginTransaction();
        try {
            scanner.scan(mavenProject, mavenProject.getFile().getAbsolutePath(), MavenScope.PROJECT);
        } finally {
            store.commitTransaction();
        }
    }

}
