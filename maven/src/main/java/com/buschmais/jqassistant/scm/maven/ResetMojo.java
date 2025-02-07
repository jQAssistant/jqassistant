package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Resets the store.
 */
@Mojo(name = "reset", requiresProject = false, aggregator = true, threadSafe = true)
public class ResetMojo extends AbstractProjectMojo {

    @Override
    protected void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        withStore(store -> store.reset(), mojoExecutionContext);
    }
}
