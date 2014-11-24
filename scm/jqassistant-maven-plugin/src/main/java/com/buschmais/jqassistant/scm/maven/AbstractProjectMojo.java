package com.buschmais.jqassistant.scm.maven;

import java.util.*;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Abstract base class for mojos which are executed per project.
 */
public abstract class AbstractProjectMojo extends AbstractMojo {

    /**
     * A marker for an already executed goal of a project.
     */
    private static class ExecutionKey {

        private String goal;

        private String execution;

        /**
         * Constructor.
         * 
         * @param mojoExecution
         *            The mojo execution as provided by Maven.
         */
        private ExecutionKey(MojoExecution mojoExecution) {
            this.goal = mojoExecution.getGoal();
            this.execution = mojoExecution.getExecutionId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ExecutionKey))
                return false;

            ExecutionKey that = (ExecutionKey) o;

            if (!execution.equals(that.execution))
                return false;
            if (!goal.equals(that.goal))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = goal.hashCode();
            result = 31 * result + execution.hashCode();
            return result;
        }
    }

    /**
     * Contains the full list of projects in the reactor.
     */
    @Parameter(property = "reactorProjects")
    protected List<MavenProject> reactorProjects;

    @Parameter(property = "mojoExecution")
    protected MojoExecution execution;

    @Override
    public final void doExecute() throws MojoExecutionException, MojoFailureException {
        Map<MavenProject, List<MavenProject>> modules = getModules(reactorProjects);
        final MavenProject rootModule = ProjectResolver.getRootModule(currentProject, rulesDirectory);
        final List<MavenProject> currentModules = modules.get(rootModule);

        execute(new StoreOperation() {
            @Override
            public void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException {
                Set<MavenProject> executedModules = getExecutedModules(rootModule);
                executedModules.add(currentProject);
                if (currentModules != null && currentModules.size() == executedModules.size()) {
                    aggregate(rootModule, currentModules, store);
                }
            }
        }, rootModule);
    }

    /**
     * Determine the already executed modules for a given root module.
     * 
     * @param rootModule
     *            The root module.
     * @return The set of already executed modules belonging to the root module.
     */
    private Set<MavenProject> getExecutedModules(MavenProject rootModule) {
        ExecutionKey key = new ExecutionKey(execution);
        Map<ExecutionKey, Set<MavenProject>> executedProjectsPerExecutionKey = (Map<ExecutionKey, Set<MavenProject>>) rootModule
                .getContextValue(AbstractProjectMojo.class.getName());
        if (executedProjectsPerExecutionKey == null) {
            executedProjectsPerExecutionKey = new HashMap<>();
            rootModule.setContextValue(AbstractProjectMojo.class.getName(), executedProjectsPerExecutionKey);
        }
        Set<MavenProject> executedProjects = executedProjectsPerExecutionKey.get(key);
        if (executedProjects == null) {
            executedProjects = new HashSet<>();
            executedProjectsPerExecutionKey.put(key, executedProjects);
        }
        return executedProjects;
    }

    /**
     * Aggregate projects to their base projects
     * 
     * @param reactorProjects
     *            The current reactor projects.
     * @return A map containing resolved base projects and their aggregated
     *         projects.
     * @throws MojoExecutionException
     *             If aggregation fails.
     */
    private Map<MavenProject, List<MavenProject>> getModules(List<MavenProject> reactorProjects) throws MojoExecutionException {
        Map<MavenProject, List<MavenProject>> rootModules = new HashMap<>();
        for (MavenProject reactorProject : reactorProjects) {
            MavenProject rootModule = ProjectResolver.getRootModule(reactorProject, rulesDirectory);
            List<MavenProject> modules = rootModules.get(rootModule);
            if (modules == null) {
                modules = new ArrayList<>();
                rootModules.put(rootModule, modules);
            }
            modules.add(reactorProject);
        }
        return rootModules;
    }

    /**
     * Execute the aggregated analysis.
     * 
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If execution fails.
     * @throws org.apache.maven.plugin.MojoFailureException
     *             If execution fails.
     */
    protected abstract void aggregate(MavenProject rootModule, List<MavenProject> modules, Store store) throws MojoExecutionException, MojoFailureException;

}
