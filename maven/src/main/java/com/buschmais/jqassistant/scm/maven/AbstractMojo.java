package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.inject.Inject;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;

import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.provider.PluginRepositoryProvider;
import com.buschmais.jqassistant.scm.maven.provider.StoreFactory;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    /**
     * A marker for an already executed goal of a project.
     */
    private static class ExecutionKey {

        private String goal;

        private String execution;

        /**
         * Constructor.
         *
         * @param mojoExecution The mojo execution as provided by Maven.
         */
        private ExecutionKey(MojoExecution mojoExecution) {
            this.goal = mojoExecution.getGoal();
            this.execution = mojoExecution.getExecutionId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ExecutionKey))
                return false;

            ExecutionKey that = (ExecutionKey) o;

            return execution.equals(that.execution) && goal.equals(that.goal);
        }

        @Override
        public int hashCode() {
            int result = goal.hashCode();
            result = 31 * result + execution.hashCode();
            return result;
        }
    }

    public static final String REPORT_XML = "jqassistant-report.xml";

    public static final String PROPERTY_STORE_LIFECYCLE = "jqassistant.store.lifecycle";

    /**
     * The store directory.
     */
    @Parameter(property = "jqassistant.store.directory")
    protected File storeDirectory;

    /**
     * Determines if the execution root module shall be used as project root, i.e. to create the store and read the rules from.
     */
    @Parameter(property = "jqassistant.useExecutionRootAsProjectRoot")
    protected boolean useExecutionRootAsProjectRoot = false;

    /**
     * Specifies the name of the directory containing rule files. It is also
     * used to identify the root module.
     */
    @Parameter(property = "jqassistant.rules.directory", defaultValue = ProjectResolver.DEFAULT_RULES_DIRECTORY)
    protected String rulesDirectory;

    /**
     * Specifies a list of directory names relative to the root module
     * containing additional rule files.
     */
    @Parameter(property = "jqassistant.rules.directories")
    protected List<String> rulesDirectories;

    /**
     * The url to retrieve rules.
     */
    @Parameter(property = "jqassistant.rules.url")
    protected URL rulesUrl;

    /**
     * The list of concept names to be applied.
     */
    @Parameter(property = "jqassistant.concepts")
    protected List<String> concepts;

    /**
     * The list of constraint names to be validated.
     */
    @Parameter(property = "jqassistant.constraints")
    protected List<String> constraints;

    /**
     * The list of group names to be executed.
     */
    @Parameter(property = "jqassistant.groups")
    protected List<String> groups;

    /**
     * The file to write the XML report to.
     */
    @Parameter(property = "jqassistant.report.xml")
    protected File xmlReportFile;

    /**
     * Skip the execution.
     */
    @Parameter(property = "jqassistant.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Controls the life cycle of the data store.
     * <p>
     * REACTOR is the default value which provides caching of the initialized
     * store. There are configurations where this will cause problems, in such
     * cases MODULE shall be used.
     * </p>
     */
    @Parameter(property = PROPERTY_STORE_LIFECYCLE)
    protected StoreLifecycle storeLifecycle = StoreLifecycle.REACTOR;

    /**
     * The Maven project.
     */
    @Parameter(property = "project")
    protected MavenProject currentProject;

    /**
     * Contains the full list of projects in the reactor.
     */
    @Parameter(property = "reactorProjects")
    protected List<MavenProject> reactorProjects;

    /**
     * The current execution.
     */
    @Parameter(property = "mojoExecution")
    protected MojoExecution execution;

    @Inject
    protected PluginRepositoryProvider pluginRepositoryProvider;

    /**
     * The Maven runtime information.
     */
    @Component
    private RuntimeInformation runtimeInformation;

    /**
     * The store repository.
     */
    @Inject
    private StoreFactory storeFactory;

    /**
     * The rules reader instance.
     */
    private RuleSetReader ruleSetReader = new CompoundRuleSetReader();

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (!runtimeInformation.isMavenVersion("[3.2,)")) {
            throw new MojoExecutionException("jQAssistant requires Maven 3.2.x or above.");
        }
        MavenProject rootModule = ProjectResolver.getRootModule(execution, currentProject, reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
        Set<MavenProject> executedModules = getExecutedModules(rootModule);
        execute(rootModule, executedModules);
        executedModules.add(currentProject);
    }

    /**
     * Execute the mojo.
     *
     * @param rootModule      The root module of the project.
     * @param executedModules The already executed modules of the project.
     * @throws MojoExecutionException If a general execution problem occurs.
     * @throws MojoFailureException   If a failure occurs.
     */
    protected abstract void execute(MavenProject rootModule, Set<MavenProject> executedModules) throws MojoExecutionException, MojoFailureException;

    /**
     * Determine if the store shall be reset before execution of the mofo.
     *
     * @return <code>true</code> if the store shall be reset.
     */
    protected abstract boolean isResetStoreBeforeExecution();

    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A rule set. .
     * @throws MojoExecutionException If the rules cannot be read.
     */
    protected RuleSet readRules(MavenProject rootModule) throws MojoExecutionException {
        List<RuleSource> sources = new ArrayList<>();
        if (rulesUrl != null) {
            getLog().debug("Retrieving rules from URL " + rulesUrl.toString());
            sources.add(new UrlRuleSource(rulesUrl));
        } else {
            // read rules from rules directory
            addRuleFiles(sources, ProjectResolver.getRulesDirectory(rootModule, rulesDirectory));
            if (rulesDirectories != null) {
                for (String directory : rulesDirectories) {
                    addRuleFiles(sources, ProjectResolver.getRulesDirectory(rootModule, directory));
                }
            }
            List<RuleSource> ruleSources = pluginRepositoryProvider.getRulePluginRepository().getRuleSources();
            sources.addAll(ruleSources);
        }
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        try {
            ruleSetReader.read(sources, ruleSetBuilder);
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot read rules.", e);
        }
        return ruleSetBuilder.getRuleSet();
    }

    /**
     * Add rules from the given directory to the list of sources.
     *
     * @param sources   The sources.
     * @param directory The directory.
     * @throws MojoExecutionException On error.
     */
    private void addRuleFiles(List<RuleSource> sources, File directory) throws MojoExecutionException {
        List<RuleSource> ruleSources = readRulesDirectory(directory);
        for (RuleSource ruleSource : ruleSources) {
            getLog().debug("Adding rules from file " + ruleSource);
            sources.add(ruleSource);
        }
    }

    /**
     * Retrieves the list of available rules from the rules directory.
     *
     * @param rulesDirectory The rules directory.
     * @return The {@link java.util.List} of available rules
     * {@link java.io.File}s.
     * @throws MojoExecutionException If the rules directory cannot be read.
     */
    private List<RuleSource> readRulesDirectory(File rulesDirectory) throws MojoExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new MojoExecutionException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
        getLog().info("Reading rules from directory " + rulesDirectory.getAbsolutePath());
        try {
            return FileRuleSource.getRuleSources(rulesDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read rulesDirectory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    /**
     * Execute an operation with the store.
     * <p>
     * This method enforces thread safety based on the store factory.
     * </p>
     *
     * @param storeOperation The store.
     * @param rootModule     The root module to use for store initialization.
     * @throws MojoExecutionException On execution errors.
     * @throws MojoFailureException   On execution failures.
     */
    protected void execute(StoreOperation storeOperation, MavenProject rootModule, Set<MavenProject> executedModules) throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution.");
        } else {
            synchronized (storeFactory) {
                Store store = getStore(rootModule);
//              boolean shouldReset = reactorProjects.contains(rootModule) ? currentProject.equals(rootModule) : currentProject.isExecutionRoot();
                if (isResetStoreBeforeExecution() && executedModules.isEmpty()) {
                    store.reset();
                }
                try {
                    storeOperation.run(rootModule, store);
                } finally {
                    releaseStore(rootModule, store);
                }
            }
        }
    }

    /**
     * Determine the already executed modules for a given root module.
     *
     * @param rootModule The root module.
     * @return The set of already executed modules belonging to the root module.
     */
    protected Set<MavenProject> getExecutedModules(MavenProject rootModule) {
        ExecutionKey key = new ExecutionKey(execution);
        String executedModulesContextKey = AbstractProjectMojo.class.getName() + "#executedModules";
        Map<ExecutionKey, Set<MavenProject>> executedProjectsPerExecutionKey =
                (Map<ExecutionKey, Set<MavenProject>>) rootModule.getContextValue(executedModulesContextKey);
        if (executedProjectsPerExecutionKey == null) {
            executedProjectsPerExecutionKey = new HashMap<>();
            rootModule.setContextValue(executedModulesContextKey, executedProjectsPerExecutionKey);
        }
        Set<MavenProject> executedProjects = executedProjectsPerExecutionKey.get(key);
        if (executedProjects == null) {
            executedProjects = new HashSet<>();
            executedProjectsPerExecutionKey.put(key, executedProjects);
        }
        return executedProjects;
    }

    /**
     * Determine the store instance to use for the given root module.
     *
     * @param rootModule The root module.
     * @return The store instance.
     * @throws MojoExecutionException If the store cannot be opened.
     */
    private Store getStore(MavenProject rootModule) throws MojoExecutionException {
        Store store = null;
        switch (storeLifecycle) {
            case MODULE:
                break;
            case REACTOR:
                Object existingStore = rootModule.getContextValue(Store.class.getName());
                if (existingStore != null) {
                    if (!Store.class.isAssignableFrom(existingStore.getClass())) {
                        throw new MojoExecutionException(
                                "Cannot re-use store instance from reactor. Either declare the plugin as extension or execute Maven using the property -D"
                                        + PROPERTY_STORE_LIFECYCLE + "=" + StoreLifecycle.MODULE + " on the command line.");
                    }
                    store = (Store) existingStore;
                }
                break;
        }
        if (store == null) {
            File directory = getStoreDirectory(rootModule);
            List<Class<?>> descriptorTypes;
            try {
                descriptorTypes = pluginRepositoryProvider.getModelPluginRepository().getDescriptorTypes();
            } catch (PluginRepositoryException e) {
                throw new MojoExecutionException("Cannot determine model types.", e);
            }
            store = storeFactory.createStore(directory, descriptorTypes);
        }
        return store;
    }

    /**
     * Release a store instance.
     *
     * @param rootModule The root module
     * @param store      The store instance.
     */
    private void releaseStore(MavenProject rootModule, Store store) {
        switch (storeLifecycle) {
            case MODULE:
                storeFactory.closeStore(store);
                break;
            case REACTOR:
                rootModule.setContextValue(Store.class.getName(), store);
                break;
        }
    }

    /**
     * Determines the directory to use for the store.
     *
     * @param rootModule The root module.
     * @return The directory.
     */
    private File getStoreDirectory(MavenProject rootModule) {
        File directory;
        if (this.storeDirectory != null) {
            directory = this.storeDirectory;
        } else {
            directory = new File(rootModule.getBuild().getDirectory() + "/jqassistant/store");
        }
        return directory;
    }

    /**
     * Defines an operation to execute on an initialized store instance.
     */
    protected interface StoreOperation {
        /**
         * Execute the operation-
         *
         * @param store      The store.
         * @param rootModule The root module.
         * @throws MojoExecutionException On execution errors.
         * @throws MojoFailureException   On execution failures.
         */
        void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException;
    }
}
