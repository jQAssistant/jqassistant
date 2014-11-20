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
                Set<MavenProject> executedModules = getExecutedProjects(rootModule);
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
    private Set<MavenProject> getExecutedProjects(MavenProject rootModule) {
        String key = execution.getExecutionId();
        Map<String, Set<MavenProject>> executedProjectsPerExecutionId = (Map<String, Set<MavenProject>>) rootModule.getContextValue(AbstractProjectMojo.class
                .getName());
        if (executedProjectsPerExecutionId == null) {
            executedProjectsPerExecutionId = new HashMap<>();
            rootModule.setContextValue(AbstractProjectMojo.class.getName(), executedProjectsPerExecutionId);
        }
        Set<MavenProject> executedProjects = executedProjectsPerExecutionId.get(key);
        if (executedProjects == null) {
            executedProjects = new HashSet<>();
            executedProjectsPerExecutionId.put(key, executedProjects);
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
