package com.buschmais.jqassistant.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base implementation for analysis mojos running as aggregator.
 */
public abstract class AbstractAnalysisAggregatorMojo extends AbstractAnalysisMojo {

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        MavenProject lastProject = reactorProjects.get(reactorProjects.size() - 1);
        if (currentProject.equals(lastProject)) {
            Map<MavenProject, Set<MavenProject>> baseProjects = new HashMap<>();
            for (MavenProject reactorProject : reactorProjects) {
                MavenProject baseProject = BaseProjectResolver.getBaseProject(reactorProject);
                Set<MavenProject> projects = baseProjects.get(baseProject);
                if (projects == null) {
                    projects = new HashSet<>();
                    baseProjects.put(baseProject, projects);
                }
                projects.add(reactorProject);
            }
            for (Map.Entry<MavenProject, Set<MavenProject>> entry: baseProjects.entrySet()) {
                aggregate(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Execute the aggregated analysis.
     *
     * @throws MojoExecutionException If execution fails.
     * @throws MojoFailureException   If execution fails.
     */
    protected abstract void aggregate(MavenProject baseProject, Set<MavenProject> projects) throws MojoExecutionException, MojoFailureException;
}
