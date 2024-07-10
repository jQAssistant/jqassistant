package com.buschmais.jqassistant.scm.maven;

import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract base class for mojos which are executed per module.
 */
public abstract class AbstractModuleMojo extends AbstractMojo {

    @Override
    public final void execute(MojoExecutionContext mojoExecutionContext, Set<MavenProject> executedModules) throws MojoExecutionException, MojoFailureException {
        if (mojoExecutionContext.getConfiguration()
            .maven()
            .module()
            .skip()) {
            getLog().info("Skipping module.");
        } else {
            execute(mojoExecutionContext);
        }
    }

    protected abstract void execute(MojoExecutionContext mojoExecutionContext)
        throws MojoExecutionException, MojoFailureException;

}
