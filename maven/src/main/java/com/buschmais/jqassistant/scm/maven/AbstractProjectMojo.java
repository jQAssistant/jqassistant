package com.buschmais.jqassistant.scm.maven;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract base class for mojos which are executed per project.
 */
public abstract class AbstractProjectMojo extends AbstractMojo {

    @Override
    public final void execute(final MavenProject rootModule, final Set<MavenProject> executedModules) throws MojoExecutionException,
            MojoFailureException {
        Map<MavenProject, List<MavenProject>> projects =
                ProjectResolver.getProjects(reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
        final List<MavenProject> projectModules = projects.get(rootModule);
        boolean isLastModuleInProject = isLastModuleInProject(projectModules);
        getLog().debug(
                "Verifying if '" + currentProject + "' is last module for project '" + rootModule + "': " + isLastModuleInProject
                        + (" (project modules='" + projectModules + "')."));
        if (isLastModuleInProject) {
            execute(new StoreOperation() {
                @Override
                public void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException {
                    aggregate(rootModule, projectModules, store);
                }
            }, rootModule, executedModules);
        }
    }

    /**
     * Determines if the last module for a project is currently executed.
     *
     * @param projectModules  The modules of the project.
     * @return <code>true</code> if the current module is the last of the project.
     */
    private boolean isLastModuleInProject(List<MavenProject> projectModules) {
        // The project modules are in execution order; take advantage of that.
        int currentProjectIndex = projectModules.indexOf(currentProject);
        int remainingModulesPossiblyExecutingPlugin = 0;
        for (int i = currentProjectIndex + 1 ; i < projectModules.size() ; ++i) {
            MavenProject followingModule = projectModules.get(i);
            if (ProjectResolver.containsBuildPlugin(followingModule, execution.getPlugin())) {
                remainingModulesPossiblyExecutingPlugin++;
            }
        }
        if (remainingModulesPossiblyExecutingPlugin > 0) {
            getLog().debug(
                    "Found " + remainingModulesPossiblyExecutingPlugin
                    + " subsequent modules possibly executing this plugin."
                    + " Will NOT consider this module as the last one."
            );
            return false;
        } else {
            getLog().debug(
                    "Did not find any subsequent module with a plugin configuration."
                    + " Will consider this module as the last one."
            );
            return true;
        }
    }

    /**
     * Execute the aggregated analysis.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException If execution fails.
     * @throws org.apache.maven.plugin.MojoFailureException   If execution fails.
     */
    protected abstract void aggregate(MavenProject rootModule, List<MavenProject> modules, Store store) throws MojoExecutionException,
            MojoFailureException;

}
