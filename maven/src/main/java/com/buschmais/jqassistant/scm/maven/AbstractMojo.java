package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.configuration.Embedded;
import com.buschmais.jqassistant.scm.maven.configuration.EmbeddedNeo4jConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.RuleConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.StoreConfiguration;
import com.buschmais.jqassistant.scm.maven.provider.CachingStoreProvider;
import com.buschmais.jqassistant.scm.maven.provider.ConfigurationProvider;
import com.buschmais.jqassistant.scm.maven.provider.PluginRepositoryProvider;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import static com.buschmais.jqassistant.core.shared.option.OptionHelper.coalesce;
import static java.util.Optional.empty;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    public static final String PARAMETER_EMBEDDED_LISTEN_ADDRESS = "jqassistant.embedded.listenAddress";
    public static final String PARAMETER_EMBEDDED_BOLT_PORT = "jqassistant.embedded.boltPort";
    public static final String PARAMETER_EMBEDDED_HTTP_PORT = "jqassistant.embedded.httpPort";

    public static final String STORE_DIRECTORY = "jqassistant/store";

    private static String createExecutionKey(MojoExecution mojoExecution) {
        // Do NOT use a custom class for execution keys, as different modules may use
        // different classloaders
        return mojoExecution.getGoal() + "@" + mojoExecution.getExecutionId();
    }

    public static final String PROPERTY_STORE_LIFECYCLE = "jqassistant.store.lifecycle";

    /**
     * The store directory.
     */
    @Parameter(property = "jqassistant.store.directory")
    protected File storeDirectory;

    /**
     * The store url.
     */
    @Parameter(property = "jqassistant.store.uri")
    protected URI storeUri;

    /**
     * The store user name.
     */
    @Parameter(property = "jqassistant.store.username")
    protected String storeUserName;

    /**
     * The store password.
     */
    @Parameter(property = "jqassistant.store.password")
    protected String storePassword;

    /**
     * The store encryption.
     */
    @Parameter(property = "jqassistant.store.encryption")
    protected String storeEncryption;

    /**
     * The store trust strategy.
     */
    @Parameter(property = "jqassistant.store.trustStrategy")
    protected String storeTrustStrategy;

    /**
     * The store trust certificate.
     */
    @Parameter(property = "jqassistant.store.trustCertificate")
    protected String storeTrustCertificate;

    /**
     * The store configuration.
     */
    @Parameter
    protected StoreConfiguration store = new StoreConfiguration();

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
    private RuleConfiguration rule = new RuleConfiguration();

    /**
     * Determines if the execution root module shall be used as project root, i.e.
     * to create the store and read the rules from.
     */
    @Parameter(property = "jqassistant.useExecutionRootAsProjectRoot")
    protected boolean useExecutionRootAsProjectRoot = false;

    /**
     * Specifies the name of the directory containing rule files. It is also used to
     * identify the root module.
     */
    @Parameter(property = "jqassistant.rules.directory", defaultValue = ProjectResolver.DEFAULT_RULES_DIRECTORY)
    protected String rulesDirectory;

    /**
     * Specifies a list of directory names relative to the root module containing
     * additional rule files.
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
     * {@link StoreLifecycle#REACTOR} is the default value which provides caching of
     * the initialized store. There are configurations where this will cause
     * problems, in such cases {@link StoreLifecycle#MODULE} shall be used.
     *
     */
    @Parameter(property = PROPERTY_STORE_LIFECYCLE)
    protected StoreLifecycle storeLifecycle = StoreLifecycle.REACTOR;

    /**
     * The Maven Session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

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

    /**
     * The Maven runtime information.
     */
    @Component
    private RuntimeInformation runtimeInformation;

    @Component
    private ConfigurationProvider configurationProvider;

    @Component
    private PluginRepositoryProvider pluginRepositoryProvider;

    /**
     * The store repository.
     */
    @Component
    private CachingStoreProvider cachingStoreProvider;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter( defaultValue = "${repositorySystemSession}", readonly = true, required = true )
    private RepositorySystemSession repositorySystemSession;

    @Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true )
    private List<RemoteRepository> repositories;


    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (!runtimeInformation.isMavenVersion("[3.5,)")) {
            throw new MojoExecutionException("jQAssistant requires Maven 3.5.x or above.");
        }
        // Synchronize on this class as multiple instances of the plugin may exist in
        // parallel builds
        synchronized (AbstractMojo.class) {
            MavenConfiguration configuration = getConfiguration();
            MavenProject rootModule = ProjectResolver.getRootModule(currentProject, reactorProjects, rulesDirectory,
                configuration.useExecutionRootAsProjectRoot());
            Set<MavenProject> executedModules = getExecutedModules(rootModule);
            if (skip) {
                getLog().info("Skipping execution.");
            } else {
                execute(rootModule, executedModules, configuration);
            }
            executedModules.add(currentProject);
        }
    }

    /**
     * Execute the mojo.
     *
     * @param rootModule
     *     The root module of the project.
     * @param executedModules
     *     The already executed modules of the project.
     * @param configuration
     *     The {@link Configuration}.
     * @throws MojoExecutionException
     *     If a general execution problem occurs.
     * @throws MojoFailureException
     *     If a failure occurs.
     */
    protected abstract void execute(MavenProject rootModule, Set<MavenProject> executedModules, Configuration configuration) throws MojoExecutionException, MojoFailureException;

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
     * @throws MojoExecutionException
     *             If the rules cannot be read.
     */
    protected final RuleSet readRules(MavenProject rootModule, Configuration configuration) throws MojoExecutionException {
        List<RuleSource> sources = new ArrayList<>();
        PluginRepository pluginRepository = getPluginRepository(configuration);
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
            ruleParserPlugins = pluginRepository.getRulePluginRepository()
                .getRuleParserPlugins(configuration.analyze()
                    .rule());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot get rules rule source reader plugins.", e);
        }
        try {
            RuleParser ruleParser = new RuleParser(ruleParserPlugins);
            return ruleParser.parse(sources);
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot read rules.", e);
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
     * @param rulesDirectory
     *            The rules directory.
     * @return The {@link java.util.List} of available rules {@link java.io.File}s.
     * @throws MojoExecutionException
     *             If the rules directory cannot be read.
     */
    private List<RuleSource> readRulesDirectory(File rulesDirectory) throws MojoExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new MojoExecutionException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
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
     *
     * @param storeOperation
     *     The store.
     * @param rootModule
     *     The root module to use for store initialization.
     * @param executedModules
     *     The set of already executed modules.
     * @param configuration
     *     The {@link Configuration}.
     * @throws MojoExecutionException
     *     On execution errors.
     * @throws MojoFailureException
     *     On execution failures.
     */
    protected final void execute(StoreOperation storeOperation, MavenProject rootModule, Set<MavenProject> executedModules, Configuration configuration)
        throws MojoExecutionException, MojoFailureException {
        Store store = getStore(configuration, () -> coalesce(this.storeDirectory, new File(rootModule.getBuild()
            .getDirectory(), STORE_DIRECTORY)));
        if (isResetStoreBeforeExecution() && executedModules.isEmpty()) {
            store.reset();
        }
        try {
            storeOperation.run(rootModule, store, configuration);
        } finally {
            releaseStore(store);
        }
    }

    /**
     * Determine the already executed modules for a given root module.
     *
     * @param rootModule
     *            The root module.
     * @return The set of already executed modules belonging to the root module.
     */
    private Set<MavenProject> getExecutedModules(MavenProject rootModule) {
        String executionKey = createExecutionKey(execution);
        String executedModulesContextKey = AbstractProjectMojo.class.getName() + "#executedModules";
        Map<String, Set<MavenProject>> executedProjectsPerExecutionKey = (Map<String, Set<MavenProject>>) rootModule.getContextValue(executedModulesContextKey);
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
     * @return The store instance.
     * @throws MojoExecutionException
     *             If the store cannot be opened.
     */
    private Store getStore(Configuration configuration, Supplier<File> storeDirectorySupplier) throws MojoExecutionException {
        Object existingStore = cachingStoreProvider.getStore(configuration.store(), storeDirectorySupplier, getPluginRepository(configuration));
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
     * @param store
     *            The store instance.
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
     * Retrieve the runtime configuration.
     * <p>
     * The configuration directory is assumed to be located within the execution root of the Maven session.
     *
     * @return The {@link Configuration}.
     */
    private MavenConfiguration getConfiguration() throws MojoExecutionException {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("MojoConfigSource", 110);
        addStoreConfiguration(configurationBuilder);
        addConfigurationProperties(configurationBuilder);
        File executionRoot = new File(session.getExecutionRootDirectory());
        return configurationProvider.getConfiguration(executionRoot, empty(), configurationBuilder.build());
    }

    /**
     * Apply store configuration.
     *
     * @param configurationBuilder
     *     The {@link ConfigurationBuilder}.
     */
    private void addStoreConfiguration(ConfigurationBuilder configurationBuilder) {
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
            com.buschmais.jqassistant.core.store.api.configuration.Store.URI, coalesce(storeUri, store.getUri()));
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
            com.buschmais.jqassistant.core.store.api.configuration.Store.USERNAME, coalesce(storeUserName, store.getUsername()));
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
            com.buschmais.jqassistant.core.store.api.configuration.Store.PASSWORD, coalesce(storePassword, store.getPassword()));
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
            com.buschmais.jqassistant.core.store.api.configuration.Store.ENCRYPTION, coalesce(storeEncryption, store.getEncryption()));
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
            com.buschmais.jqassistant.core.store.api.configuration.Store.TRUST_STRATEGY, coalesce(storeTrustStrategy, store.getTrustStrategy()));
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
            com.buschmais.jqassistant.core.store.api.configuration.Store.TRUST_CERTIFICATE, coalesce(storeTrustCertificate, store.getTrustCertificate()));
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
            com.buschmais.jqassistant.core.store.api.configuration.Store.PROPERTIES, store.getProperties());

        EmbeddedNeo4jConfiguration embedded = store.getEmbedded();
        configurationBuilder.with(Embedded.PREFIX, Embedded.CONNECTORY_ENABLED, coalesce(embedded.getConnectorEnabled(), isConnectorRequired()));
        configurationBuilder.with(Embedded.PREFIX, Embedded.LISTEN_ADDRESS, coalesce(embeddedListenAddress, embedded.getListenAddress()));
        configurationBuilder.with(Embedded.PREFIX, Embedded.BOLT_PORT, coalesce(embeddedBoltPort, embedded.getBoltPort()));
        configurationBuilder.with(Embedded.PREFIX, Embedded.HTTP_PORT, coalesce(embeddedHttpPort, embedded.getHttpPort()));
    }

    /**
     * Method to be overridden by sub-classes to add configuration properties.
     *
     * @param configurationBuilder
     *     The {@link ConfigurationBuilder}.
     */
    protected void addConfigurationProperties(ConfigurationBuilder configurationBuilder) throws MojoExecutionException {
        configurationBuilder.with(MavenConfiguration.PREFIX, MavenConfiguration.USE_EXECUTION_ROOT_AS_PROJECT_ROOT, useExecutionRootAsProjectRoot);
        if (storeDirectory != null) {
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.PREFIX,
                com.buschmais.jqassistant.core.store.api.configuration.Store.URI, storeDirectory.toURI());
        }
        configurationBuilder.with(Rule.PREFIX, Rule.DEFAULT_CONCEPT_SEVERITY, rule.getDefaultConceptSeverity());
        configurationBuilder.with(Rule.PREFIX, Rule.DEFAULT_CONSTRAINT_SEVERITY, rule.getDefaultConstraintSeverity());
        configurationBuilder.with(Rule.PREFIX, Rule.DEFAULT_GROUP_SEVERITY, rule.getDefaultGroupSeverity());
    }

    /**
     * Retrieve the {@link PluginRepository}.
     *
     * @return the {@link PluginRepository}.
     */
    protected PluginRepository getPluginRepository(Configuration configuration) {
        return pluginRepositoryProvider.getPluginRepository(repositorySystem, repositorySystemSession, repositories, configuration.plugins());
    }

    /**
     * Defines an operation to execute on an initialized store instance.
     */
    protected interface StoreOperation {
        /**
         * Execute the operation-
         *
         * @param rootModule
         *     The root module.
         * @param store
         *     The store.
         * @param configuration
         *     The {@link Configuration}.
         * @throws MojoExecutionException
         *     On execution errors.
         * @throws MojoFailureException
         *     On execution failures.
         */
        void run(MavenProject rootModule, Store store, Configuration configuration) throws MojoExecutionException, MojoFailureException;
    }

}
