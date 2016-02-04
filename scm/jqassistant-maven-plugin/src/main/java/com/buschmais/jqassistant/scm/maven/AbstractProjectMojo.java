package com.buschmais.jqassistant.scm.maven;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Abstract base class for mojos which are executed per project.
 */
public abstract class AbstractProjectMojo extends AbstractMojo {

    @Override
    public final void execute(final MavenProject rootModule, final Set<MavenProject> executedModules) throws MojoExecutionException,
            MojoFailureException {
        Map<MavenProject, List<MavenProject>> modules =
                ProjectResolver.getRootModules(reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
        final List<MavenProject> currentModules = modules.get(rootModule);
        boolean isLastModuleInProject = currentModules != null && currentModules.size() == executedModules.size() + 1;
        getLog().debug(
                "Verifying if '" + currentProject + "' is last module for project '" + rootModule + "': " + isLastModuleInProject
                        + (" (project modules='" + currentModules + "')."));
        if (isLastModuleInProject) {
            execute(new StoreOperation() {
                @Override
                public void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException {
                    aggregate(rootModule, currentModules, store);
                }
            }, rootModule, executedModules);
        }
    }

    /**
     * Execute the aggregated analysis.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If execution fails.
     * @throws org.apache.maven.plugin.MojoFailureException
     *             If execution fails.
     */
    protected abstract void aggregate(MavenProject rootModule, List<MavenProject> modules, Store store) throws MojoExecutionException,
            MojoFailureException;

}
