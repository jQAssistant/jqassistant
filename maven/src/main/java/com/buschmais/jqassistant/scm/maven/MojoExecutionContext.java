package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import lombok.Getter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Resolver for root modules in a multi-module hierarchy.
 */
@Getter
public final class MojoExecutionContext {

    /**
     * The name of the rules directory.
     */
    public static final String DEFAULT_RULES_DIRECTORY = "jqassistant";

    /**
     * The name of the rules directory.
     */
    public static final String OUTPUT_DIRECTORY = "jqassistant";

    private final MavenSession mavenSession;

    private final MavenProject currentModule;

    private final MavenProject rootModule;

    private final MojoExecution mojoExecution;

    private final MavenConfiguration configuration;

    private final PluginRepository pluginRepository;

    MojoExecutionContext(MavenSession session, MavenProject currentModule, MojoExecution mojoExecution, MavenConfiguration configuration,
        PluginRepository pluginRepository) throws MojoExecutionException {
        this.mavenSession = session;
        this.currentModule = currentModule;
        this.mojoExecution = mojoExecution;
        this.configuration = configuration;
        this.pluginRepository = pluginRepository;
        this.rootModule = getRootModule(currentModule, session.getProjects());
    }

    /**
     * Return the {@link MavenProject} which is the base module for scanning
     * and analysis.
     * <p>
     * The base module is by searching with the module tree starting from the current module over its parents until a module is found containing a
     * directory "jqassistant" or no parent can be determined.
     *
     * @param module
     *     The current module.
     * @return The {@link MavenProject} containing a rules directory.
     * @throws MojoExecutionException
     *     If the directory cannot be resolved.
     */
    private MavenProject getRootModule(MavenProject module, List<MavenProject> reactor) throws MojoExecutionException {
        String rootModuleContextKey = MojoExecutionContext.class.getName() + "#rootModule";
        MavenProject rootModule = (MavenProject) module.getContextValue(rootModuleContextKey);
        if (rootModule == null) {
            if (configuration.maven()
                .useExecutionRootAsProjectRoot()) {
                rootModule = getRootModule(reactor);
            } else {
                rootModule = getRootModule(module);
            }
            module.setContextValue(rootModuleContextKey, rootModule);
        }
        return rootModule;
    }

    private MavenProject getRootModule(List<MavenProject> reactor) throws MojoExecutionException {
        for (MavenProject mavenProject : reactor) {
            if (mavenProject.isExecutionRoot()) {
                return mavenProject;
            }
        }
        throw new MojoExecutionException("Cannot determine execution root.");
    }

    private MavenProject getRootModule(MavenProject module) {
        File directory = getDirectory(module, getRuleDirectoryName());
        if (directory.exists() && directory.isDirectory()) {
            return module;
        }
        MavenProject parent = module.getParent();
        if (parent != null && parent.getBasedir() != null) {
            return getRootModule(parent);
        }
        return module;
    }

    /**
     * Aggregate projects to their base projects
     *
     * @return A map containing resolved base projects and their aggregated projects.
     * @throws MojoExecutionException
     *     If aggregation fails.
     */
    Map<MavenProject, List<MavenProject>> getProjects() throws MojoExecutionException {
        Map<MavenProject, List<MavenProject>> rootModules = new HashMap<>();
        for (MavenProject reactorProject : mavenSession.getProjects()) {
            MavenProject rootModule = getRootModule(reactorProject, mavenSession.getProjects());
            rootModules.computeIfAbsent(rootModule, module -> new ArrayList<>())
                .add(reactorProject);
        }
        return rootModules;
    }

    /**
     * Returns the primary rule directory.
     *
     * @return The file representing the directory.
     */
    File getRuleDirectory() {
        String directoryName = getRuleDirectoryName();
        return getRuleDirectory(directoryName);
    }

    /**
     * Returns the rule directory with the given name.
     *
     * @return The file representing the directory.
     */
    File getRuleDirectory(String directoryName) {
        return getDirectory(rootModule, directoryName);
    }

    /**
     * Resolve a directory.
     * <p>
     * If the directory name is absolute then it is returned as is, otherwise resolved against the given {@link MavenProject}.
     *
     * @param module
     *     The {@link MavenProject}.
     * @param directoryName
     *     The directory name.
     * @return The directory.
     */
    private File getDirectory(MavenProject module, String directoryName) {
        File directory = new File(directoryName);
        return directory.isAbsolute() ? directory : new File(module.getBasedir(), directoryName);
    }

    private String getRuleDirectoryName() {
        return configuration.analyze()
            .rule()
            .directory()
            .orElse(DEFAULT_RULES_DIRECTORY);
    }

    /**
     * Determines if the given plugin is a build plugin of a maven project.
     *
     * @param project
     *     The project.
     * @param plugin
     *     The plugin
     * @return <code>true</code> if the project uses the plugim.
     */
    boolean containsBuildPlugin(MavenProject project, Plugin plugin) {
        return project.getBuildPlugins()
            .contains(plugin);
    }

    /**
     * Determines the directory for writing output files.
     *
     * @return The report directory.
     */
    File getOutputDirectory() {
        String directoryName = rootModule.getBuild()
            .getDirectory() + "/" + OUTPUT_DIRECTORY;
        File directory = new File(directoryName);
        directory.mkdirs();
        return directory;
    }

    /**
     * Determines a report file name.
     *
     * @param reportFile
     *     The report file as specified in the pom.xml file or on the command line.
     * @return The resolved {@link java.io.File}.
     * @throws MojoExecutionException
     *     If the file cannot be determined.
     */
    File getOutputFile(File reportFile, String defaultFile) throws MojoExecutionException {
        File selectedXmlReportFile;
        if (reportFile != null) {
            selectedXmlReportFile = reportFile;
        } else if (rootModule != null) {
            selectedXmlReportFile = new File(getOutputDirectory() + "/" + defaultFile);
        } else {
            throw new MojoExecutionException("Cannot determine report file.");
        }
        return selectedXmlReportFile;
    }

}
