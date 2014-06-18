package com.buschmais.jqassistant.scm.maven;

import static com.buschmais.jqassistant.core.scanner.api.iterable.IterableConsumer.consume;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.pluginrepository.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.api.MavenScope;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ScanMojo extends AbstractAnalysisMojo {

    @Override
    protected void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        for (MavenProject project : projects) {
            List<ScannerPlugin<?>> scannerPlugins;
            ScannerPluginRepository scannerPluginRepository = getScannerPluginRepository(store, getPluginProperties(project));
            try {
                scannerPlugins = scannerPluginRepository.getScannerPlugins();
            } catch (PluginRepositoryException e) {
                throw new MojoExecutionException("Cannot determine scanner plugins.", e);
            }
            Scanner scanner = new ScannerImpl(scannerPlugins);
            try {
                consume(scanner.scan(project, project.getFile().getAbsolutePath(), MavenScope.PROJECT));
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot scan project '" + project.getBasedir() + "'", e);
            }
        }
    }

    @Override
    protected boolean isResetStoreOnInitialization() {
        return true;
    }
}
