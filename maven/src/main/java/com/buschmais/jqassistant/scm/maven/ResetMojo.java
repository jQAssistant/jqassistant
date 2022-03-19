package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Resets the store.
 */
@Mojo(name = "reset", aggregator = true, requiresProject = false, threadSafe = true, configurator = "custom")
public class ResetMojo extends AbstractModuleMojo {

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    protected void execute(MojoExecutionContext mojoExecutionContext, MavenProject mavenProject) throws MojoExecutionException, MojoFailureException {
        withStore(store -> store.reset(), mojoExecutionContext);
    }

}
