package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.shared.option.OptionHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;
import com.buschmais.jqassistant.scm.maven.provider.CachingStoreProvider;
import com.buschmais.jqassistant.scm.maven.provider.PluginRepositoryProvider;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;

import static com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration.DEFAULT;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    public static final String PARAMETER_EMBEDDED_LISTEN_ADDRESS = "jqassistant.embedded.listenAddress";
    public static final String PARAMETER_EMBEDDED_BOLT_PORT = "jqassistant.embedded.boltPort";
    public static final String PARAMETER_EMBEDDED_HTTP_PORT = "jqassistant.embedded.httpPort";

    public static final String STORE_DIRECTORY = "jqassistant/store";

    private static String createExecutionKey(MojoExecution mojoExecution) {
        // Do NOT use a custom class for execution keys, as different modules may use different classloaders
        return mojoExecution.getGoal() + "@" + mojoExecution.getExecutionId();
    }

    public static final String PROPERTY_STORE_LIFECYCLE = "jqassistant.store.lifecycle";

    /**
     * The store directory.
     */
    @Parameter(property = "jqassistant.store.directory")
    protected File storeDirectory;

    /**
     * The store configuration.
     */
    @Parameter
    protected StoreConfiguration store = StoreConfiguration.builder().build();

    /**
     * The listen address of the embedded server.
     */
    @Parameter(property = PARAMETER_EMBEDDED_LISTEN_ADDRESS)
    protected String embeddedListenAddress;

    /**
     * The bolt port of the embedded server.
     */
    @Parameter(property = PARAMETER_EMBEDDED_BOLT_PORT)
    protected Integer embeddedBoltPort;

    /**
     * The http port of the embedded server.
     */
    @Parameter(property = PARAMETER_EMBEDDED_HTTP_PORT)
    protected Integer embeddedHttpPort;

    /**
     * The rule configuration
     */
    @Parameter
    private RuleConfiguration rule;

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
     * The URL to retrieve rules.
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
     *
     * {@link StoreLifecycle#REACTOR} is the default value which provides caching of the initialized
     * store. There are configurations where this will cause problems, in such
     * cases {@link StoreLifecycle#MODULE} shall be used.
     *
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

    @Component
    protected PluginRepositoryProvider pluginRepositoryProvider;

    /**
     * The Maven runtime information.
     */
    @Component
    private RuntimeInformation runtimeInformation;

    /**
     * The store repository.
     */
    @Component
    private CachingStoreProvider cachingStoreProvider;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (!runtimeInformation.isMavenVersion("[3.5,)")) {
            throw new MojoExecutionException("jQAssistant requires Maven 3.5.x or above.");
        }
        MavenProject rootModule = ProjectResolver.getRootModule(currentProject, reactorProjects, rulesDirectory, useExecutionRootAsProjectRoot);
        Set<MavenProject> executedModules = getExecutedModules(rootModule);
        if (skip) {
            getLog().info("Skipping execution.");
        } else {
            execute(rootModule, executedModules);
        }
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
     * @return `true` if the store shall be reset.
     */
    protected abstract boolean isResetStoreBeforeExecution();

    /**
     * Determines if the executed MOJO requires enabled connectors.
     *
     * @return <code>true</code> If connectors must be enabled.
     */
    protected abstract boolean isConnectorRequired();

    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A rule set. .
     * @throws MojoExecutionException If the rules cannot be read.
     */
    protected RuleSet readRules(MavenProject rootModule) throws MojoExecutionException {
        List<RuleSource> sources = new ArrayList<>();
        PluginRepository pluginRepository = pluginRepositoryProvider.getPluginRepository();
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
            List<RuleSource> ruleSources = pluginRepository.getRulePluginRepository().getRuleSources();
            sources.addAll(ruleSources);
        }
        Collection<RuleParserPlugin> ruleParserPlugins;
        try {
            ruleParserPlugins = pluginRepository.getRulePluginRepository().getRuleParserPlugins(getRuleConfiguration());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot get rules rule source reader plugins.", e);        }
        try {
            RuleParser ruleParser = new RuleParser(ruleParserPlugins);
            return ruleParser.parse(sources);
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot read rules.", e);
        }
    }

    protected RuleConfiguration getRuleConfiguration() {
        Severity defaultConceptSeverity = DEFAULT.getDefaultConceptSeverity();
        Severity defaultConstraintSeverity = DEFAULT.getDefaultConstraintSeverity();
        Severity defaultGroupSeverity = DEFAULT.getDefaultGroupSeverity();

        if (rule != null) {
            defaultConceptSeverity = rule.getDefaultConceptSeverity();
            defaultConstraintSeverity = rule.getDefaultConstraintSeverity();
            defaultGroupSeverity = rule.getDefaultGroupSeverity();
        }

        Severity effectiveConceptSeverity = defaultConceptSeverity != null ? defaultConceptSeverity : DEFAULT.getDefaultConceptSeverity();
        Severity effectiveConstraintSeverity = defaultConstraintSeverity != null ? defaultConstraintSeverity : DEFAULT.getDefaultConstraintSeverity();
        Severity effectiveGroupSeverity = defaultGroupSeverity != null ? defaultGroupSeverity : DEFAULT.getDefaultGroupSeverity();

        return RuleConfiguration.builder()
                                .defaultConceptSeverity(effectiveConceptSeverity)
                                .defaultConstraintSeverity(effectiveConstraintSeverity)
                                .defaultGroupSeverity(effectiveGroupSeverity)
                                .build();
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
     *
     * This method enforces thread safety based on the store factory.
     *
     * @param storeOperation The store.
     * @param rootModule     The root module to use for store initialization.
     * @throws MojoExecutionException On execution errors.
     * @throws MojoFailureException   On execution failures.
     */
    protected void execute(StoreOperation storeOperation, MavenProject rootModule, Set<MavenProject> executedModules) throws MojoExecutionException, MojoFailureException {
            synchronized (cachingStoreProvider) {
                Store store = getStore(rootModule);
                if (isResetStoreBeforeExecution() && executedModules.isEmpty()) {
                    store.reset();
                }
                try {
                    storeOperation.run(rootModule, store);
                } finally {
                    releaseStore(store);
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
        String executionKey = createExecutionKey( execution );
        String executedModulesContextKey = AbstractProjectMojo.class.getName() + "#executedModules";
        Map<String, Set<MavenProject>> executedProjectsPerExecutionKey =
                (Map<String, Set<MavenProject>>) rootModule.getContextValue(executedModulesContextKey);
        if (executedProjectsPerExecutionKey == null) {
            executedProjectsPerExecutionKey = new HashMap<>();
            rootModule.setContextValue(executedModulesContextKey, executedProjectsPerExecutionKey);
        }
        Set<MavenProject> executedProjects = executedProjectsPerExecutionKey.get(executionKey);
        if (executedProjects == null) {
            executedProjects = new HashSet<>();
            executedProjectsPerExecutionKey.put(executionKey, executedProjects);
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
        StoreConfiguration configuration = getStoreConfiguration(rootModule);
        PluginRepository pluginRepository = pluginRepositoryProvider.getPluginRepository();
        Object existingStore = cachingStoreProvider.getStore(configuration, pluginRepository);
        if (!Store.class.isAssignableFrom(existingStore.getClass())) {
            throw new MojoExecutionException(
                    "Cannot re-use store instance from reactor. Either declare the plugin as extension or execute Maven using the property -D"
                            + PROPERTY_STORE_LIFECYCLE + "=" + StoreLifecycle.MODULE + " on the command line.");
        }
        return (Store) existingStore;
    }

    /**
     * Release a store instance.
     *
     * @param store      The store instance.
     */
    private void releaseStore(Store store) {
        switch (storeLifecycle) {
            case MODULE:
                cachingStoreProvider.closeStore(store);
                break;
            case REACTOR:
                break;
        }
    }

    /**
     * Creates the {@link StoreConfiguration}. This is a copy of the {@link #store}
     * enriched by default values and additional command line parameters.
     *
     * @param rootModule
     *            The root module.
     * @return The directory.
     */
    private StoreConfiguration getStoreConfiguration(MavenProject rootModule) {
        StoreConfiguration.StoreConfigurationBuilder builder = StoreConfiguration.builder();
        if (store.getUri() == null) {
            File storeDirectory = OptionHelper.selectValue(new File(rootModule.getBuild().getDirectory(), STORE_DIRECTORY), this.storeDirectory);
            storeDirectory.getParentFile().mkdirs();
            URI uri = new File(storeDirectory, "/").toURI();
            builder.uri(uri);
        } else {
            builder.uri(store.getUri());
            builder.username(store.getUsername());
            builder.password(store.getPassword());
            builder.encryption(store.getEncryption());
            builder.trustStrategy(store.getTrustStrategy());
            builder.trustCertificate(store.getTrustCertificate());
        }
        builder.properties(store.getProperties());
        builder.embedded(getEmbeddedNeo4jConfiguration());
        StoreConfiguration storeConfiguration = builder.build();
        getLog().debug("Using store configuration " + storeConfiguration);
        return storeConfiguration;
    }

    /**
     * Create the configuration for the embedded server.
     */
    private EmbeddedNeo4jConfiguration getEmbeddedNeo4jConfiguration() {
        EmbeddedNeo4jConfiguration embedded = store.getEmbedded();
        EmbeddedNeo4jConfiguration.EmbeddedNeo4jConfigurationBuilder builder = EmbeddedNeo4jConfiguration.builder();
        builder.connectorEnabled(embedded.isConnectorEnabled() || isConnectorRequired());
        builder.listenAddress(OptionHelper.selectValue(embedded.getListenAddress(), embeddedListenAddress));
        builder.boltPort(OptionHelper.selectValue(embedded.getBoltPort(), embeddedBoltPort));
        builder.httpPort(OptionHelper.selectValue(embedded.getHttpPort(), embeddedHttpPort));
        return builder.build();
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
