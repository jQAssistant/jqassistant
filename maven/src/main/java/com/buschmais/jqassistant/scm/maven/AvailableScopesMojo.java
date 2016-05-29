package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.plugin.api.ScopePluginRepository;
import com.buschmais.jqassistant.core.splittingsupport.scm.ScopeHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lists all available scopes.
 */
@Mojo(name = "available-scopes", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class AvailableScopesMojo extends AbstractProjectMojo {

    private Logger logger = LoggerFactory.getLogger(AvailableScopesMojo.class);

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Available scopes for '" + rootModule.getName() + "'.");
        ScopeHelper scopeHelper = new ScopeHelper(logger);
        ScopePluginRepository scopePluginRepository = pluginRepositoryProvider.getScopePluginRepository();
        scopeHelper.printScopes(scopePluginRepository.getScopes());
    }
}
