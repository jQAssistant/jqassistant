package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.scanner.api.configuration.Include;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
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

import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = TEST, threadSafe = true, configurator = "custom")
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

    @Override
    protected void addConfigurationProperties(ConfigurationBuilder configurationBuilder) throws MojoExecutionException {
        super.addConfigurationProperties(configurationBuilder);
        configurationBuilder.with(Scan.class, Scan.CONTINUE_ON_ERROR, continueOnError)
            .with(Scan.class, Scan.RESET, reset)
            .with(Scan.class, Scan.PROPERTIES, scanProperties);
        // Convert scan includes
        List<String> files = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        for (ScanInclude scanInclude : scanIncludes) {
            String path = scanInclude.getPath();
            URL url = scanInclude.getUrl();
            String scope = scanInclude.getScope();
            StringBuilder builder = new StringBuilder();
            if (scope != null) {
                builder.append(scope).append(ScopeHelper.SCOPE_SEPARATOR);
            }
            if (path != null) {
                files.add(builder.append(path).toString());
            } else if (url !=null) {
                urls.add(builder.append(url).toString());
            } else {
                throw new MojoExecutionException(
                    "A scanInclude can only include either a file or an URL: path=" + scanInclude.getPath() + ", url=" + scanInclude.getUrl());
            }
        }
        configurationBuilder.with(Include.class, Include.FILES, files);
        configurationBuilder.with(Include.class, Include.URLS, urls);
    }

    @Override
    public void execute(MavenProject mavenProject, Store store, Configuration configuration) throws MojoExecutionException {
        ScannerPluginRepository scannerPluginRepository = getPluginRepository(configuration).getScannerPluginRepository();
        ScannerContext scannerContext = new ScannerContextImpl(store, ProjectResolver.getOutputDirectory(mavenProject));
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
