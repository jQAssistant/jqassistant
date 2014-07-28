package com.buschmais.jqassistant.scm.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Resolver for base projects in a multi-module hierarchy.
 */
public final class ProjectResolver {

    /**
     * The name of the rules directory.
     */
    public static final String RULES_DIRECTORY = "jqassistant";

    /**
     * The name of the rules directory.
     */
    public static final String OUTPUT_DIRECTORY = "jqassistant";

    /**
     * Private constructor.
     */
    private ProjectResolver() {
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
    static MavenProject getRootModule(MavenProject project) throws MojoExecutionException {
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
     * Determines the directory for writing output files.
     * 
     * @param baseProject
     *            The base project.
     * @return The report directory.
     */
    static File getOutputDirectory(MavenProject baseProject) {
        String directoryName = baseProject.getBuild().getDirectory() + "/" + OUTPUT_DIRECTORY;
        File directory = new File(directoryName);
        directory.mkdirs();
        return directory;
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
    static File getOutputFile(MavenProject baseProject, File reportFile, String defaultFile) throws MojoExecutionException {
        File selectedXmlReportFile;
        if (reportFile != null) {
            selectedXmlReportFile = reportFile;
        } else if (baseProject != null) {
            selectedXmlReportFile = new File(getOutputDirectory(baseProject) + "/" + defaultFile);
        } else {
            throw new MojoExecutionException("Cannot determine report file.");
        }
        return selectedXmlReportFile;
    }

}
