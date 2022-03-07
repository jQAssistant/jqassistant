package com.buschmais.jqassistant.scm.maven;

import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all available scopes.
 */
@Mojo(name = "available-scopes", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true,
      configurator = "custom")
public class AvailableScopesMojo extends AbstractProjectMojo {

    private Logger logger = LoggerFactory.getLogger(AvailableScopesMojo.class);

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException {
        getLog().info("Available scopes for '" + rootModule.getName() + "'.");
        ScopeHelper scopeHelper = new ScopeHelper(logger);
        ScannerPluginRepository scannerPluginRepository = getPluginRepository().getScannerPluginRepository();
        scopeHelper.printScopes(scannerPluginRepository.getScopes());
    }
}
