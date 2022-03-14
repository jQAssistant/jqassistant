package com.buschmais.jqassistant.scm.maven;

import java.util.Set;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract base class for mojos which are executed per module.
 */
public abstract class AbstractModuleMojo extends AbstractMojo {

    @Override
    public final void execute(MavenProject rootModule, Set<MavenProject> executedModules, Configuration configuration)
        throws MojoExecutionException, MojoFailureException {
        StoreOperation storeOperation = (root, store, config) -> execute(currentProject, store, config);
        execute(storeOperation, rootModule, executedModules, configuration);
    }

    protected abstract void execute(MavenProject mavenProject, Store store, Configuration configuration) throws MojoExecutionException, MojoFailureException;

}
