package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all available scopes.
 */
@Mojo(name = "available-scopes", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class AvailableScopesMojo extends AbstractProjectMojo {

    private Logger logger = LoggerFactory.getLogger(AvailableScopesMojo.class);

    @Override
    public void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException {
        getLog().info("Available scopes for '" + mojoExecutionContext.getRootModule()
            .getName() + "'.");
        ScopeHelper scopeHelper = new ScopeHelper(logger);
        ScannerPluginRepository scannerPluginRepository = mojoExecutionContext.getPluginRepository()
            .getScannerPluginRepository();
        scopeHelper.printScopes(scannerPluginRepository.getScopes());
    }
}
