package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolver for root modules in a multi-module hierarchy.
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
     * Return the {@link MavenProject} which is the base module for scanning and analysis.
     * <p>
     * The base module is by searching with the module tree starting from the current module over its parents until a module is found containing a
     * directory "jqassistant" or no parent can be determined.
     * </p>
     *
     * @param module                        The current module.
     * @param rulesDirectory                The name of the directory used for identifying the root module.
     * @param useExecutionRootAsProjectRoot <code>true</code> if the execution root shall be used as project root.
     * @return The {@link MavenProject} containing a rules directory.
     * @throws MojoExecutionException If the directory cannot be resolved.
     */
    static MavenProject getRootModule(MavenProject module, List<MavenProject> reactor, String rulesDirectory, boolean useExecutionRootAsProjectRoot)
            throws MojoExecutionException {
        String rootModuleContextKey = ProjectResolver.class.getName() + "#rootModule";
        MavenProject rootModule = (MavenProject) module.getContextValue(rootModuleContextKey);
        if (rootModule == null) {
            if (useExecutionRootAsProjectRoot) {
                rootModule = getRootModule(reactor);
            } else {
                rootModule = getRootModule(module, rulesDirectory);
            }
            module.setContextValue(rootModuleContextKey, rootModule);
        }
        return rootModule;
    }

    private static MavenProject getRootModule(List<MavenProject> reactor) throws MojoExecutionException {
        for (MavenProject mavenProject : reactor) {
            if (mavenProject.isExecutionRoot()) {
                return mavenProject;
            }
        }
        throw new MojoExecutionException("Cannot determine execution root.");
    }

    private static MavenProject getRootModule(MavenProject module, String rulesDirectory) throws MojoExecutionException {
        MavenProject rootModule;
        File directory = getRulesDirectory(module, rulesDirectory);
        if (directory.exists() && directory.isDirectory()) {
            rootModule = module;
        } else {
            MavenProject parent = module.getParent();
            if (parent != null && parent.getBasedir() != null) {
                rootModule = getRootModule(parent, rulesDirectory);
            } else {
                rootModule = module;
            }
        }
        return rootModule;
    }

    /**
     * Aggregate projects to their base projects
     *
     * @param reactorProjects               The current reactor projects.
     * @param rulesDirectory                The configured rules directory.
     * @param useExecutionRootAsProjectRoot <code>true</code> if the execution root shall be used as project root.
     * @return A map containing resolved base projects and their aggregated projects.
     * @throws MojoExecutionException If aggregation fails.
     */
    static Map<MavenProject, List<MavenProject>> getProjects(List<MavenProject> reactorProjects,
                                                             String rulesDirectory, boolean useExecutionRootAsProjectRoot) throws MojoExecutionException {
        Map<MavenProject, List<MavenProject>> rootModules = new HashMap<>();
        for (MavenProject reactorProject : reactorProjects) {
            MavenProject rootModule =
                    ProjectResolver.getRootModule(reactorProject, reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
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
     * Determines if the given plugin is a build plugin of a maven project.
     *
     * @param project The project.
     * @param plugin  The plugin
     * @return <code>true</code> if the project uses the plugim.
     */
    static boolean containsBuildPlugin(MavenProject project, Plugin plugin) {
        return project.getBuildPlugins().contains(plugin);
    }

    /**
     * Returns the directory containing rules.
     *
     * @param rootModule     The root module of the project.
     * @param rulesDirectory The name of the directory used for identifying the root module.
     * @return The file representing the directory.
     */
    static File getRulesDirectory(MavenProject rootModule, String rulesDirectory) {
        File rules = new File(rulesDirectory);
        return rules.isAbsolute() ? rules : new File(rootModule.getBasedir().getAbsolutePath() + File.separator + rulesDirectory);
    }

    /**
     * Determines the directory for writing output files.
     *
     * @param rootModule The root module of the project.
     * @return The report directory.
     */
    static File getOutputDirectory(MavenProject rootModule) {
        String directoryName = rootModule.getBuild().getDirectory() + "/" + OUTPUT_DIRECTORY;
        File directory = new File(directoryName);
        directory.mkdirs();
        return directory;
    }

    /**
     * Determines a report file name.
     *
     * @param rootModule The base project.
     * @param reportFile The report file as specified in the pom.xml file or on the command line.
     * @return The resolved {@link java.io.File}.
     * @throws MojoExecutionException If the file cannot be determined.
     */
    static File getOutputFile(MavenProject rootModule, File reportFile, String defaultFile) throws MojoExecutionException {
        File selectedXmlReportFile;
        if (reportFile != null) {
            selectedXmlReportFile = reportFile;
        } else if (rootModule != null) {
            selectedXmlReportFile = new File(getOutputDirectory(rootModule) + "/" + defaultFile);
        } else {
            throw new MojoExecutionException("Cannot determine report file.");
        }
        return selectedXmlReportFile;
    }

}
