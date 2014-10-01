package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.provider.PluginConfigurationProvider;
import com.buschmais.jqassistant.scm.maven.provider.StoreFactory;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    public static final String REPORT_XML = "jqassistant-report.xml";

    /**
     * The store directory.
     */
    @Parameter(property = "jqassistant.store.directory")
    protected File storeDirectory;

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
    @Parameter(property = "jqassistant.store.lifecycle")
    protected StoreLifecycle storeLifecycle = StoreLifecycle.REACTOR;

    /**
     * The Maven project.
     */
    @Parameter(property = "project")
    protected MavenProject currentProject;

    @Component
    protected PluginConfigurationProvider pluginRepositoryProvider;

    /**
     * The store repository.
     */
    @Component
    private StoreFactory storeFactory;

    /**
     * The rules reader instance.
     */
    private RuleSetReader ruleSetReader = new RuleSetReaderImpl();

    /**
     * The rules selector.
     */
    private RuleSelector ruleSelector = new RuleSelectorImpl();

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution.");
        } else {
            doExecute();
        }
    }

    /**
     * Execute the mojo.
     * 
     * @throws MojoExecutionException
     *             If a general execution problem occurs.
     * @throws MojoFailureException
     *             If a failure occurs.
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    /**
     * Determine if the store shall be reset before execution of the mofo.
     * 
     * @return <code>true</code> if the store shall be reset.
     */
    protected abstract boolean isResetStoreBeforeExecution();

    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A
     *         {@link com.buschmais.jqassistant.core.analysis.api.rule.RuleSet}.
     * @throws MojoExecutionException
     *             If the rules cannot be read.
     */
    protected RuleSet readRules(MavenProject rootModule) throws MojoExecutionException {
        List<Source> sources = new ArrayList<>();
        // read rules from rules directory
        addRuleFiles(sources, ProjectResolver.getRulesDirectory(rootModule, rulesDirectory));
        if (rulesDirectories != null) {
            for (String directory : rulesDirectories) {
                addRuleFiles(sources, ProjectResolver.getRulesDirectory(rootModule, directory));
            }
        }
        if (rulesUrl != null) {
            try {
                getLog().debug("Adding rules from URL " + rulesUrl.toString());
                sources.add(new StreamSource(rulesUrl.openStream(), rulesUrl.toExternalForm()));
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot open rule URL " + rulesUrl.toExternalForm());
            }
        }
        List<Source> ruleSources = pluginRepositoryProvider.getRulePluginRepository().getRuleSources();
        sources.addAll(ruleSources);
        return ruleSetReader.read(sources);
    }

    /**
     * Resolves the effective rules.
     *
     * @return The resolved rules set.
     * @throws MojoExecutionException
     *             If resolving fails.
     */
    protected RuleSet resolveEffectiveRules(MavenProject baseProject) throws MojoExecutionException {
        RuleSet ruleSet = readRules(baseProject);
        validateRuleSet(ruleSet);
        try {
            return ruleSelector.getEffectiveRuleSet(ruleSet, concepts, constraints, groups);
        } catch (RuleSetResolverException e) {
            throw new MojoExecutionException("Cannot resolve rules.", e);
        }
    }

    /**
     * Add rules from the given directory to the list of sources.
     *
     * @param sources
     *            The sources.
     * @param directory
     *            The directory.
     * @throws MojoExecutionException
     *             On error.
     */
    private void addRuleFiles(List<Source> sources, File directory) throws MojoExecutionException {
        List<File> ruleFiles = readRulesDirectory(directory);
        for (File ruleFile : ruleFiles) {
            getLog().debug("Adding rules from file " + ruleFile.getAbsolutePath());
            sources.add(new StreamSource(ruleFile));
        }
    }

    /**
     * Retrieves the list of available rules from the rules directory.
     *
     * @param rulesDirectory
     *            The rules directory.
     * @return The {@link java.util.List} of available rules
     *         {@link java.io.File}s.
     * @throws MojoExecutionException
     *             If the rules directory cannot be read.
     */
    private List<File> readRulesDirectory(File rulesDirectory) throws MojoExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new MojoExecutionException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
        getLog().info("Reading rules from directory " + rulesDirectory.getAbsolutePath());
        final List<File> ruleFiles = new ArrayList<>();
        try {
            new DirectoryWalker<File>() {

                @Override
                protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                    if (file.getName().endsWith(".xml")) {
                        results.add(file);
                    }
                }

                public void scan(File directory) throws IOException {
                    super.walk(directory, ruleFiles);
                }
            }.scan(rulesDirectory);
            return ruleFiles;
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read rulesDirectory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    /**
     * Validates the given rules set for unresolved concepts, constraints or
     * groups.
     *
     * @param ruleSet
     *            The rules set.
     * @throws MojoExecutionException
     *             If there are unresolved concepts, constraints or groups.
     */
    private void validateRuleSet(RuleSet ruleSet) throws MojoExecutionException {
        StringBuffer message = new StringBuffer();
        if (!ruleSet.getMissingConcepts().isEmpty()) {
            message.append("\n  Concepts: ");
            message.append(ruleSet.getMissingConcepts());
        }
        if (!ruleSet.getMissingConstraints().isEmpty()) {
            message.append("\n  Constraints: ");
            message.append(ruleSet.getMissingConstraints());
        }
        if (!ruleSet.getMissingGroups().isEmpty()) {
            message.append("\n  Groups: ");
            message.append(ruleSet.getMissingGroups());
        }
        if (message.length() > 0) {
            throw new MojoExecutionException("The following rules are referenced but are not available;" + message);
        }
    }

    /**
     * Execute an operation with the store.
     *
     * @param storeOperation
     *            The store.
     * @throws MojoExecutionException
     *             On execution errors.
     * @throws MojoFailureException
     *             On execution failures.
     */
    protected void execute(StoreOperation storeOperation) throws MojoExecutionException, MojoFailureException {
        MavenProject rootModule = ProjectResolver.getRootModule(currentProject, rulesDirectory);
        execute(storeOperation, rootModule);
    }

    /**
     * Execute an operation with the store.
     * 
     * @param storeOperation
     *            The store.
     * @param rootModule
     *            The root module to use for store initialization.
     * @throws MojoExecutionException
     *             On execution errors.
     * @throws MojoFailureException
     *             On execution failures.
     */
    protected void execute(StoreOperation storeOperation, MavenProject rootModule) throws MojoExecutionException, MojoFailureException {
        Store store = null;
        switch (storeLifecycle) {
        case MODULE:
            break;
        case REACTOR:
            Object existingStore = rootModule.getContextValue(Store.class.getName());
            if (existingStore != null) {
                if (!Store.class.isAssignableFrom(existingStore.getClass())) {
                    throw new MojoExecutionException("Cannot re-use cached store instance, switch to store life cycle " + StoreLifecycle.MODULE);
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
        if (isResetStoreBeforeExecution() && currentProject.isExecutionRoot()) {
            store.reset();
        }
        try {
            storeOperation.run(rootModule, store);
        } finally {
            switch (storeLifecycle) {
            case MODULE:
                storeFactory.closeStore(store);
                break;
            case REACTOR:
                rootModule.setContextValue(Store.class.getName(), store);
                break;
            }
        }
    }

    /**
     * Determines the directory to use for the store.
     * 
     * @param rootModule
     *            The root module.
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
         * @param store
         *            The store.
         * @param rootModule
         *            The root module.
         * @throws MojoExecutionException
         *             On execution errors.
         * @throws MojoFailureException
         *             On execution failures.
         */
        void run(MavenProject rootModule, Store store) throws MojoExecutionException, MojoFailureException;
    }
}
