package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.util.Set;

/**
 * Abstract base class for mojos which are executed per module.
 */
public abstract class AbstractModuleMojo extends AbstractMojo {

    @Override
    public final void execute(MavenProject rootModule, Set<MavenProject> executedModules) throws MojoExecutionException, MojoFailureException {
        StoreOperation storeOperation = new StoreOperation() {
            @Override
            public void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException {
                execute(currentProject, store);
            }
        };
        execute(storeOperation, rootModule, executedModules);
    }

    protected abstract void execute(MavenProject mavenProject, Store store) throws MojoExecutionException, MojoFailureException;

}
