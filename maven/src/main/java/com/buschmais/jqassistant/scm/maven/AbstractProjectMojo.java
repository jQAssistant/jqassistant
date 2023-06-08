package com.buschmais.jqassistant.scm.maven;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
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
        boolean isLastModuleInProject = isLastModuleInProject(mojoExecutionContext, executedModules);
        if (isLastModuleInProject) {
            aggregate(mojoExecutionContext);
        }
    }

    /**
     * Determines if the last module for a project is currently executed.
     *
     * @return <code>true</code> if the current module is the last of the project.
     */
    private boolean isLastModuleInProject(MojoExecutionContext mojoExecutionContext, Set<MavenProject> executedModules) throws MojoExecutionException {
        Map<MavenProject, List<MavenProject>> projects = mojoExecutionContext.getProjects();
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        List<MavenProject> projectModules = projects.get(rootModule);
        MavenProject currentModule = mojoExecutionContext.getCurrentModule();
        getLog().debug("Verifying if '" + currentModule + "' is last module for project '" + rootModule + (" (project modules='" + projectModules + "')."));
        Set<MavenProject> remainingModules = new HashSet<>();
        MojoExecution mojoExecution = mojoExecutionContext.getMojoExecution();
        if (mojoExecution.getPlugin()
            .getExecutions()
            .isEmpty()) {
            getLog().debug("No configured executions found, assuming CLI invocation.");
            remainingModules.addAll(projectModules);
        } else {
            for (MavenProject projectModule : projectModules) {
                if (mojoExecutionContext.containsBuildPlugin(projectModule, mojoExecution.getPlugin())) {
                    remainingModules.add(projectModule);
                }
            }
        }
        remainingModules.removeAll(executedModules);
        remainingModules.remove(currentModule);
        if (remainingModules.isEmpty()) {
            getLog().debug("Did not find any subsequent module with a plugin configuration, considering this module as the last one.");
            return true;
        } else {
            getLog().debug("Found " + remainingModules.size() + " subsequent modules executing this plugin.");
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
