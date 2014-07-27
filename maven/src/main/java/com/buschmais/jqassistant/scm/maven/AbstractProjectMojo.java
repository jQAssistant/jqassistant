package com.buschmais.jqassistant.scm.maven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractProjectMojo extends AbstractMojo {

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Map<MavenProject, List<MavenProject>> baseProjects = getBaseProjects(reactorProjects);

        // Execute the goal if the current project is the last executed project
        // of a base project
        MavenProject baseProject = BaseProjectResolver.getBaseProject(currentProject);
        List<MavenProject> currentProjects = baseProjects.get(baseProject);
        if (currentProjects != null && currentProject.equals(currentProjects.get(currentProjects.size() - 1))) {
            List<Class<?>> descriptorTypes;
            Store store = getStore(baseProject);
            try {
                descriptorTypes = getScannerPluginRepository(store, getPluginProperties(baseProject)).getDescriptorTypes();
            } catch (PluginRepositoryException e) {
                throw new MojoExecutionException("Cannot get descriptor mappers.", e);
            }
            try {
                store.start(descriptorTypes);
                this.aggregate(baseProject, currentProjects, store);
            } finally {
                store.stop();
            }
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
    private Map<MavenProject, List<MavenProject>> getBaseProjects(List<MavenProject> reactorProjects) throws MojoExecutionException {
        Map<MavenProject, List<MavenProject>> baseProjects = new HashMap<>();
        for (MavenProject reactorProject : reactorProjects) {
            MavenProject baseProject = BaseProjectResolver.getBaseProject(reactorProject);
            List<MavenProject> projects = baseProjects.get(baseProject);
            if (projects == null) {
                projects = new ArrayList<>();
                baseProjects.put(baseProject, projects);
            }
            projects.add(reactorProject);
        }
        return baseProjects;
    }

    /**
     * Execute the aggregated analysis.
     * 
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If execution fails.
     * @throws org.apache.maven.plugin.MojoFailureException
     *             If execution fails.
     */
    protected abstract void aggregate(MavenProject baseProject, List<MavenProject> modules, Store store) throws MojoExecutionException, MojoFailureException;

}
