package com.buschmais.jqassistant.scm.maven;

import java.io.File;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import static com.buschmais.jqassistant.core.scanner.api.DefaultScope.NONE;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

/**
 * Scans the current Maven project.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = TEST, threadSafe = true)
public class ScanMojo extends AbstractMojo {

    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenStoreTask(cachingStoreProvider) {

            @Override
            protected boolean isResetStoreBeforeExecution(MavenConfiguration configuration) {
                return configuration.scan()
                    .reset()
                    .orElse(true);
            }

            @Override
            public void enterModule(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
                withStore(store -> scan(mavenTaskContext, store), mavenTaskContext);
            }

            private void scan(MavenTaskContext mavenTaskContext, Store store) {
                MavenProject mavenProject = mavenTaskContext.getCurrentModule();
                MavenConfiguration configuration = mavenTaskContext.getConfiguration();
                File workingDirectory = mavenProject.getBasedir();
                File outputDirectory = mavenTaskContext.getOutputDirectory();
                PluginRepository pluginRepository = mavenTaskContext.getPluginRepository();
                ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
                ScannerContext scannerContext = new ScannerContextImpl(pluginRepository.getClassLoader(), store, workingDirectory, outputDirectory);
                Scanner scanner = new ScannerImpl(configuration.scan(), scannerContext, scannerPluginRepository);
                scannerContext.push(MavenSession.class, mavenTaskContext.getMavenSession());
                scannerContext.push(DependencyGraphBuilder.class, dependencyGraphBuilder);
                try {
                    scanner.scan(mavenProject, mavenProject.getFile()
                        .getAbsolutePath(), NONE);
                } finally {
                    scannerContext.pop(DependencyGraphBuilder.class);
                    scannerContext.pop(MavenSession.class);
                }
            }

        };
    }

}
