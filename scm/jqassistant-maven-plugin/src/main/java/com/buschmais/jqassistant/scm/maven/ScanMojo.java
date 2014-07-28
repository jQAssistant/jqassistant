package com.buschmais.jqassistant.scm.maven;

import static com.buschmais.jqassistant.core.scanner.api.iterable.IterableConsumer.consume;

import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ScanMojo extends AbstractModuleMojo {

    @Override
    public void execute(MavenProject mavenProject, Store store) throws MojoExecutionException, MojoFailureException {
        List<ScannerPlugin<?>> scannerPlugins;
        ScannerPluginRepository scannerPluginRepository = pluginRepositoryProvider.getScannerPluginRepository(store, getPluginProperties(mavenProject));
        try {
            scannerPlugins = scannerPluginRepository.getScannerPlugins();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot determine scanner plugins.", e);
        }
        Scanner scanner = new ScannerImpl(scannerPlugins);
        try {
            consume(scanner.scan(mavenProject, mavenProject.getFile().getAbsolutePath(), MavenScope.PROJECT));
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot scan project '" + mavenProject.getBasedir() + "'", e);
        }
    }

}
