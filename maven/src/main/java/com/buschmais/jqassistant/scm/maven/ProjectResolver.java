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
    public static final String DEFAULT_RULES_DIRECTORY = "jqassistant";

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
     * Return the {@link MavenProject} which is the base module for scanning and
     * analysis.
     * <p>
     * The base module is by searching with the module tree starting from the
     * current module over its parents until a module is found containing a
     * directory "jqassistant" or no parent can be determined.
     * </p>
     * 
     * @param module
     *            The current module.
     * @return The {@link MavenProject} containing a rules directory.
     * @throws MojoExecutionException
     *             If the directory cannot be resolved.
     */
    static MavenProject getRootModule(MavenProject module, String rulesDirectory) throws MojoExecutionException {
        MavenProject currentModule = module;
        if (module != null) {
            do {
                File directory = getRulesDirectory(currentModule, rulesDirectory);
                if (directory.exists() && directory.isDirectory()) {
                    return currentModule;
                }
                MavenProject parent = currentModule.getParent();
                if (parent == null || parent.getBasedir() == null) {
                    return currentModule;
                }
                currentModule = parent;
            } while (currentModule != null);
        }
        throw new MojoExecutionException("Cannot resolve base directory.");
    }

    static File getRulesDirectory(MavenProject currentProject, String rulesDirectory) {
        return new File(currentProject.getBasedir().getAbsolutePath() + File.separator + rulesDirectory);
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
