package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Resets the store.
 */
@Mojo(name = "reset", aggregator = true, requiresProject = false, threadSafe = true)
public class ResetMojo extends AbstractModuleMojo {

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected void execute(MavenProject mavenProject, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Resetting store.");
        store.reset();
    }

}
