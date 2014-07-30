package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Abstract base class for mojos which are executed per module.
 */
public abstract class AbstractModuleMojo extends AbstractMojo {

    @Override
    public final void doExecute() throws MojoExecutionException, MojoFailureException {
        execute(new StoreOperation() {
            @Override
            public void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException {
                execute(currentProject, store);
            }
        });
    }

    protected abstract void execute(MavenProject mavenProject, Store store) throws MojoExecutionException, MojoFailureException;

}
