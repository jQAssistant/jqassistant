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
        if (executedModules.isEmpty()) {
            getLog().debug("No modules have been executed so far, considering this module the first one within the project.");
            beforeProject(mojoExecutionContext);
        }

        Map<MavenProject, List<MavenProject>> projects = mojoExecutionContext.getProjects();
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        List<MavenProject> projectModules = projects.get(rootModule);
        MavenProject currentModule = mojoExecutionContext.getCurrentModule();
        Set<MavenProject> remainingModules = getRemainingModules(mojoExecutionContext, projectModules);
        remainingModules.removeAll(executedModules);
        remainingModules.remove(currentModule);
        if (remainingModules.isEmpty()) {
            getLog().debug("Did not find any subsequent module with a plugin configuration, considering this module the last one within the project.");
            afterProject(mojoExecutionContext);
        }
    }

    private Set<MavenProject> getRemainingModules(MojoExecutionContext mojoExecutionContext, List<MavenProject> projectModules) {
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
        return remainingModules;
    }

    /**
     * Execute required action before the project (i.e. first Maven module)
     *
     * @param mojoExecutionContext
     *     The {@link MojoExecutionContext}
     * @throws MojoExecutionException
     *     If execution fails.
     * @throws MojoFailureException
     *     If execution fails.
     */
    protected abstract void beforeProject(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException;

    /**
     * Execute required action after the project (i.e. last Maven module)
     *
     * @param mojoExecutionContext
     *     The {@link MojoExecutionContext}
     * @throws MojoExecutionException
     *     If execution fails.
     * @throws MojoFailureException
     *     If execution fails.
     */
    protected abstract void afterProject(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException;

}
