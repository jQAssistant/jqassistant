package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Resets the store.
 */
@Mojo(name = "reset", aggregator = true, requiresProject = false, threadSafe = true)
public class ResetMojo extends AbstractProjectMojo {

    @Override
    protected void beforeProject(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        withStore(Store::reset, mojoExecutionContext);
    }

    @Override
    protected void afterProject(MojoExecutionContext mojoExecutionContext) {
        // nothing to do here
    }
}
