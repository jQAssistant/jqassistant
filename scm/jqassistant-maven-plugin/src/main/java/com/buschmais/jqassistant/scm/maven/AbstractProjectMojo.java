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
        Map<MavenProject, List<MavenProject>> projects =
                ProjectResolver.getProjects(reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
        final List<MavenProject> projectModules = projects.get(rootModule);
        boolean isLastModuleInProject = isLastModuleInProject(executedModules, projectModules);
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
     * @param executedModules The executed modules.
     * @param projectModules  The modules of the project.
     * @return <code>true</code> if the current module is the last of the project.
     */
    private boolean isLastModuleInProject(Set<MavenProject> executedModules, List<MavenProject> projectModules) {
        int modulesWithPluginConfiguration = 0;
        for (MavenProject currentModule : projectModules) {
            if (ProjectResolver.containsBuildPlugin(currentModule, execution.getPlugin())) {
                modulesWithPluginConfiguration++;
            }
        }
        int expectedModules;
        if (modulesWithPluginConfiguration > 0) {
            getLog().debug("Found " + modulesWithPluginConfiguration + " modules with a plugin configuration.");
            expectedModules = modulesWithPluginConfiguration;
        } else {
            getLog().debug("No plugin configuration found in modules for current project.");
            expectedModules = projectModules.size();
        }
        return (expectedModules == executedModules.size() + 1);
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
