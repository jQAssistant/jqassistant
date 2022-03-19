package com.buschmais.jqassistant.scm.maven;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract base class for mojos which are executed per project.
 */
public abstract class AbstractProjectMojo extends AbstractMojo {

    @Override
    public final void execute(MojoExecutionContext mojoExecutionContext, Set<MavenProject> executedModules)
        throws MojoExecutionException, MojoFailureException {
        Map<MavenProject, List<MavenProject>> projects = mojoExecutionContext.getProjects();
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        final List<MavenProject> projectModules = projects.get(rootModule);
        boolean isLastModuleInProject = isLastModuleInProject(executedModules, projectModules);
        getLog().debug(
            "Verifying if '" + currentProject + "' is last module for project '" + rootModule + "': " + isLastModuleInProject + (" (project modules='"
                + projectModules + "')."));
        if (isLastModuleInProject) {
            withStore(store -> aggregate(mojoExecutionContext), mojoExecutionContext);
        }
    }

    /**
     * Determines if the last module for a project is currently executed.
     *
     * @param projectModules
     *     The modules of the project.
     * @return <code>true</code> if the current module is the last of the project.
     */
    private boolean isLastModuleInProject(Set<MavenProject> executedModules, List<MavenProject> projectModules) {
        Set<MavenProject> remainingModules = new HashSet<>();
        if (execution.getPlugin()
            .getExecutions()
            .isEmpty()) {
            getLog().debug("No configured executions found, assuming CLI invocation.");
            remainingModules.addAll(projectModules);
        } else {
            for (MavenProject projectModule : projectModules) {
                if (MojoExecutionContext.containsBuildPlugin(projectModule, execution.getPlugin())) {
                    remainingModules.add(projectModule);
                }
            }
        }
        remainingModules.removeAll(executedModules);
        remainingModules.remove(currentProject);
        if (remainingModules.isEmpty()) {
            getLog().debug("Did not find any subsequent module with a plugin configuration." + " Will consider this module as the last one.");
            return true;
        } else {
            getLog().debug(
                "Found " + remainingModules.size() + " subsequent modules possibly executing this plugin." + " Will NOT consider this module as the last one.");
            return false;
        }
    }

    /**
     * Execute the aggregated analysis.
     *
     * @param mojoExecutionContext
     *     The {@link MojoExecutionContext}
     * @throws MojoExecutionException
     *     If execution fails.
     * @throws MojoFailureException
     *     If execution fails.
     */
    protected abstract void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException;

}
