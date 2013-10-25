package com.buschmais.jqassistant.mojo;

import java.util.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Provides a mechanism to run aggregated goals on base projects as determined
 * by {@link BaseProjectResolver}.
 */
public final class Aggregator {

	/**
	 * Private Constructor.
	 */
	private Aggregator() {
	}

	public static void execute(AggregatedGoal goal, MavenProject currentProject, List<MavenProject> reactorProjects)
			throws MojoExecutionException, MojoFailureException {
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
			for (Map.Entry<MavenProject, Set<MavenProject>> entry : baseProjects.entrySet()) {
				MavenProject baseProject = entry.getKey();
				Set<MavenProject> projects = entry.getValue();
				goal.execute(baseProject, projects);
			}
		}
	}

	/**
	 * Defines the aggregated goal.
	 */
	public interface AggregatedGoal {

		/**
		 * Executes the aggregated goal.
		 * 
		 * @param baseProject
		 *            The base project.
		 * @param projects
		 *            The aggregated projects.
		 * @throws MojoExecutionException
		 *             If the execution fails unexpectedly.
		 * @throws MojoFailureException
		 *             If the execution fails.
		 */
		void execute(MavenProject baseProject, Set<MavenProject> projects) throws MojoExecutionException, MojoFailureException;
	}

}
