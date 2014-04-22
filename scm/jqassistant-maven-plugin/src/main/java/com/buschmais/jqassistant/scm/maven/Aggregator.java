package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provides a mechanism to run aggregated goals on base projects as determined
 * by {@link BaseProjectResolver}.
 */
public final class Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Aggregator.class);

    /**
     * Private Constructor.
     */
    private Aggregator() {
    }

    public static void execute(AggregatedGoal goal, MavenProject currentProject, List<MavenProject> reactorProjects)
            throws MojoExecutionException, MojoFailureException {
        // Determine the projects which have already been executed within this reactor.
        Set<MavenProject> executedProjects = new HashSet<>();
        Iterator<MavenProject> iterator = reactorProjects.iterator();
        MavenProject current;
        do {
            current = iterator.next();
            executedProjects.add(current);
        } while (iterator.hasNext() && !currentProject.equals(current));

        Map<MavenProject, List<MavenProject>> baseProjects = getBaseProjects(reactorProjects);

        // Execute the goal if the current project is the last executed project of a base project
        MavenProject baseProject = BaseProjectResolver.getBaseProject(currentProject);
        List<MavenProject> currentProjects = baseProjects.get(baseProject);
        if (currentProjects != null && currentProject.equals(currentProjects.get(currentProjects.size() - 1))) {
            goal.execute(baseProject, new HashSet<>(currentProjects));
        }
    }

    /**
     * Aggregate projects to their base projects
     *
     * @param reactorProjects The current reactor projects.
     * @return A map containing resolved base projects and their aggregated projects.
     * @throws MojoExecutionException If aggregation fails.
     */
    private static Map<MavenProject, List<MavenProject>> getBaseProjects(List<MavenProject> reactorProjects) throws MojoExecutionException {
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
     * Defines the aggregated goal.
     */
    public interface AggregatedGoal {

        /**
         * Executes the aggregated goal.
         *
         * @param baseProject The base project.
         * @param projects    The aggregated projects.
         * @throws MojoExecutionException If the execution fails unexpectedly.
         * @throws MojoFailureException   If the execution fails.
         */
        void execute(MavenProject baseProject, Set<MavenProject> projects) throws MojoExecutionException, MojoFailureException;
    }

}
