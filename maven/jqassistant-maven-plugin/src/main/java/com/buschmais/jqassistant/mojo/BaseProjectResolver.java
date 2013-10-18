package com.buschmais.jqassistant.mojo;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Resolver for base projects in a multi-module hierarchy.
 */
public final class BaseProjectResolver {

	/**
	 * The name of the rules directory.
	 */
	public static final String RULES_DIRECTORY = "jqassistant";

	/**
	 * Private constructor.
	 */
	private BaseProjectResolver() {
	}

	/**
	 * Return the {@link MavenProject} which is the base project for scanning
	 * and analysis.
	 * <p>
	 * The base project is by searching with the project tree starting from the
	 * current project over its parents until a project is found containing a
	 * directory "jqassistant" or no parent can be determined.
	 * </p>
	 * 
	 * @param project
	 *            The current project.
	 * @return The {@link MavenProject} containing a rules directory.
	 * @throws MojoExecutionException
	 *             If the directory cannot be resolved.
	 */
	static MavenProject getBaseProject(MavenProject project) throws MojoExecutionException {
		MavenProject currentProject = project;
		if (project != null) {
			do {
				File directory = new File(currentProject.getBasedir(), RULES_DIRECTORY);
				if (directory.exists() && directory.isDirectory()) {
					return currentProject;
				}
				MavenProject parent = currentProject.getParent();
				if (parent == null || parent.getBasedir() == null) {
					return currentProject;
				}
				currentProject = parent;
			} while (currentProject != null);
		}
		throw new MojoExecutionException("Cannot resolve base directory.");
	}

	/**
	 * Determines a report file name.
	 * 
	 * @param baseProject
	 *            The base project.
	 * @param reportFile
	 *            The report file as specified in the pom.xml file or on the
	 *            command line.
	 * @return The resolved {@link java.io.File}.
	 * @throws org.apache.maven.plugin.MojoExecutionException
	 *             If the file cannot be determined.
	 */
	static File getReportFile(MavenProject baseProject, File reportFile, String defaultFile) throws MojoExecutionException {
		File selectedXmlReportFile;
		if (reportFile != null) {
			selectedXmlReportFile = reportFile;
		} else if (baseProject != null) {
			String baseProjectOutputDirectory = baseProject.getBuild().getDirectory();
			selectedXmlReportFile = new File(baseProjectOutputDirectory + defaultFile);
		} else {
			throw new MojoExecutionException("Cannot determine report file.");
		}
		return selectedXmlReportFile;
	}
}
