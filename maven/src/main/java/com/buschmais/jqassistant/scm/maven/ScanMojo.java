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
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.ScanDirectory;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ScanMojo extends AbstractModuleMojo {

    /**
     * Specifies a list of directory names relative to the root module
     * containing additional rule files.
     */
    @Parameter(property = "jqassistant.scan.directories")
    protected List<ScanDirectory> scanDirectories;

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
        properties.put(ScanDirectory.class.getName(), scanDirectories);
        return properties;
    }

    @Override
    public void execute(MavenProject mavenProject, Store store) throws MojoExecutionException, MojoFailureException {
        List<ScannerPlugin<?>> scannerPlugins;
        ScannerPluginRepository scannerPluginRepository = pluginRepositoryProvider.getScannerPluginRepository(getPluginProperties());
        try {
            scannerPlugins = scannerPluginRepository.getScannerPlugins();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot determine scanner plugins.", e);
        }
        Scanner scanner = new ScannerImpl(store, scannerPlugins);
        store.beginTransaction();
        try {
            scanner.scan(mavenProject, mavenProject.getFile().getAbsolutePath(), MavenScope.PROJECT);
        } finally {
            store.commitTransaction();
        }
    }

}
