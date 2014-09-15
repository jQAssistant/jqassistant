package com.buschmais.jqassistant.scm.maven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public final void doExecute() throws MojoExecutionException, MojoFailureException {
        Map<MavenProject, List<MavenProject>> projects = getProjects(reactorProjects);
        // Execute the goal if the current project is the last executed project
        // of a base project
        final MavenProject rootModule = ProjectResolver.getRootModule(currentProject, rulesDirectory);
        final List<MavenProject> currentProjects = projects.get(rootModule);
        if (currentProjects != null && currentProject.equals(currentProjects.get(currentProjects.size() - 1))) {
            execute(new StoreOperation() {
                @Override
                public void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException {
                    aggregate(rootModule, currentProjects, store);
                }
            }, rootModule);
        }
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
    private Map<MavenProject, List<MavenProject>> getProjects(List<MavenProject> reactorProjects) throws MojoExecutionException {
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
