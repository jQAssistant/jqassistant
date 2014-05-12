package com.buschmais.jqassistant.scm.maven;

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
import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScanner;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.FileScannerImpl;
import com.buschmais.jqassistant.core.scanner.impl.ProjectScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ScanMojo extends AbstractAnalysisMojo {

    @Override
    protected void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        for (MavenProject project : projects) {
            List<FileScannerPlugin> fileScannerPlugins;
            List<ProjectScannerPlugin> projectScannerPlugins;
            ScannerPluginRepository scannerPluginRepository = getScannerPluginRepository(store, getPluginProperties(project));
            try {
                fileScannerPlugins = scannerPluginRepository.getFileScannerPlugins();
                projectScannerPlugins = scannerPluginRepository.getProjectScannerPlugins();
            } catch (PluginRepositoryException e) {
                throw new MojoExecutionException("Cannot determine scanner plugins.", e);
            }
            FileScanner fileScanner = new FileScannerImpl(fileScannerPlugins);
            ProjectScanner projectScanner = new ProjectScannerImpl(fileScanner, projectScannerPlugins);
            try {
                projectScanner.scan();
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
