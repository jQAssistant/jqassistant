package com.buschmais.jqassistant.scm.maven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
         * @param mojoExecution The mojo execution as provided by Maven.
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
            return goal.equals(that.goal);

        }

        @Override
        public int hashCode() {
            int result = goal.hashCode();
            result = 31 * result + execution.hashCode();
            return result;
        }
    }

    @Parameter(property = "mojoExecution")
    protected MojoExecution execution;

    @Override
    public final void doExecute() throws MojoExecutionException, MojoFailureException {
        final MavenProject rootModule = ProjectResolver.getRootModule(currentProject, reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
        Map<MavenProject, List<MavenProject>> modules = ProjectResolver.getRootModules(reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
        final List<MavenProject> currentModules = modules.get(rootModule);
        Set<MavenProject> executedModules = getExecutedModules(rootModule);
        executedModules.add(currentProject);
        if (currentModules != null && currentModules.size() == executedModules.size()) {
            execute(new StoreOperation() {
                @Override
                public void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException {
                    aggregate(rootModule, currentModules, store);
                }
            }, rootModule);
        }
    }

    /**
     * Determine the already executed modules for a given root module.
     *
     * @param rootModule The root module.
     * @return The set of already executed modules belonging to the root module.
     */
    private Set<MavenProject> getExecutedModules(MavenProject rootModule) {
        ExecutionKey key = new ExecutionKey(execution);
        String executedModulesContextKey = AbstractProjectMojo.class.getName() + "#executedModules";
        Map<ExecutionKey, Set<MavenProject>> executedProjectsPerExecutionKey =
                (Map<ExecutionKey, Set<MavenProject>>) rootModule.getContextValue(executedModulesContextKey);
        if (executedProjectsPerExecutionKey == null) {
            executedProjectsPerExecutionKey = new HashMap<>();
            rootModule.setContextValue(executedModulesContextKey, executedProjectsPerExecutionKey);
        }
        Set<MavenProject> executedProjects = executedProjectsPerExecutionKey.get(key);
        if (executedProjects == null) {
            executedProjects = new HashSet<>();
            executedProjectsPerExecutionKey.put(key, executedProjects);
        }
        return executedProjects;
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
