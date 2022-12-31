package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.configuration.Remote;
import com.buschmais.jqassistant.neo4j.embedded.configuration.Embedded;
import com.buschmais.jqassistant.scm.maven.configuration.Maven;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.mojo.EmbeddedNeo4jConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.mojo.StoreConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.source.MavenProjectConfigSource;
import com.buschmais.jqassistant.scm.maven.configuration.source.SettingsConfigSource;
import com.buschmais.jqassistant.scm.maven.provider.CachingStoreProvider;
import com.buschmais.jqassistant.scm.maven.provider.ConfigurationProvider;
import com.buschmais.jqassistant.scm.maven.provider.PluginRepositoryProvider;

import io.smallrye.config.PropertiesConfigSource;
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
import static java.util.Optional.of;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

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
     * The config locations.
     */
    @Parameter(property = "jqassistant.configuration.locations")
    private List<String> configurationLocations;

    /**
     * The store directory.
     */
    @Parameter(property = "jqassistant.store.directory")
    private File storeDirectory;

    /**
     * The store url.
     */
    @Parameter(property = "jqassistant.store.uri")
    private URI storeUri;

    /**
     * The store user name.
     */
    @Parameter(property = "jqassistant.store.username")
    private String storeUserName;

    /**
     * The store password.
     */
    @Parameter(property = "jqassistant.store.password")
    private String storePassword;

    /**
     * The store encryption.
     */
    @Parameter(property = "jqassistant.store.encryption")
    private String storeEncryption;

    /**
     * The store trust strategy.
     */
    @Parameter(property = "jqassistant.store.trustStrategy")
    private String storeTrustStrategy;

    /**
     * The store trust certificate.
     */
    @Parameter(property = "jqassistant.store.trustCertificate")
    private String storeTrustCertificate;

    /**
     * The store configuration.
     */
    @Parameter
    private StoreConfiguration store = new StoreConfiguration();

    /**
     * The listen address of the embedded server.
     */
    @Parameter(property = PARAMETER_EMBEDDED_LISTEN_ADDRESS)
    private String embeddedListenAddress;

    /**
     * The bolt port of the embedded server.
     */
    @Parameter(property = PARAMETER_EMBEDDED_BOLT_PORT)
    private Integer embeddedBoltPort;

    /**
     * The http port of the embedded server.
     */
    @Parameter(property = PARAMETER_EMBEDDED_HTTP_PORT)
    private Integer embeddedHttpPort;

    /**
     * Determines if the execution root module shall be used as project root, i.e.
     * to create the store and read the rules from.
     */
    @Parameter(property = "jqassistant.useExecutionRootAsProjectRoot")
    protected Boolean useExecutionRootAsProjectRoot;

    /**
     * Specifies the name of the directory containing rule files. It is also used to
     * identify the root module.
     */
    @Parameter(property = "jqassistant.rules.directory", defaultValue = MojoExecutionContext.DEFAULT_RULES_DIRECTORY)
    private String rulesDirectory;

    /**
     * Skip the execution.
     */
    @Parameter(property = "jqassistant.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Controls the life cycle of the data store.
     * <p>
     * {@link StoreLifecycle#REACTOR} is the default value which provides caching of
     * the initialized store. There are configurations where this will cause
     * problems, in such cases {@link StoreLifecycle#MODULE} shall be used.
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

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
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
            MojoExecutionContext mojoExecutionContext = new MojoExecutionContext(session, currentProject, configuration);
            MavenProject rootModule = mojoExecutionContext.getRootModule();
            Set<MavenProject> executedModules = getExecutedModules(rootModule);
            if (configuration.skip()) {
                getLog().info("Skipping execution.");
            } else {
                if (isResetStoreBeforeExecution() && executedModules.isEmpty()) {
                    withStore(store -> store.reset(), mojoExecutionContext);
                }
                execute(mojoExecutionContext, executedModules);
            }
            executedModules.add(currentProject);
        }
    }

    /**
     * Execute the mojo.
     *
     * @param mojoExecutionContext
     *     The {@link MojoExecutionContext}.
     * @param executedModules
     *     The already executed modules of the project.
     * @throws MojoExecutionException
     *     If a general execution problem occurs.
     * @throws MojoFailureException
     *     If a failure occurs.
     */
    protected abstract void execute(MojoExecutionContext mojoExecutionContext, Set<MavenProject> executedModules)
        throws MojoExecutionException, MojoFailureException;

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
     * Execute an operation with the store.
     * <p>
     * This method enforces thread safety based on the store factory.
     *
     * @param storeOperation
     *     The store.
     * @throws MojoExecutionException
     *     On execution errors.
     * @throws MojoFailureException
     *     On execution failures.
     */
    protected final void withStore(StoreOperation storeOperation, MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        MavenConfiguration configuration = mojoExecutionContext.getConfiguration();
        Store store = getStore(configuration, () -> storeDirectory != null ?
            storeDirectory :
            new File(rootModule.getBuild()
                .getDirectory(), STORE_DIRECTORY));
        try {
            storeOperation.run(store);
        } finally {
            releaseStore(store);
        }
    }

    /**
     * Determine the already executed modules for a given root module.
     *
     * @param rootModule
     *     The root module.
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
     *     If the store cannot be opened.
     */
    private Store getStore(MavenConfiguration configuration, Supplier<File> storeDirectorySupplier) throws MojoExecutionException {
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
     *     The store instance.
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
     * @return The {@link MavenConfiguration}.
     */
    private MavenConfiguration getConfiguration() throws MojoExecutionException {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("MojoConfigSource", 110);
        addStoreConfiguration(configurationBuilder);
        configure(configurationBuilder);
        File executionRoot = new File(session.getExecutionRootDirectory());
        MavenProjectConfigSource projectConfigSource = new MavenProjectConfigSource(currentProject);
        SettingsConfigSource settingsConfigSource = new SettingsConfigSource(session.getSettings());
        PropertiesConfigSource projectPropertiesConfigSource = new PropertiesConfigSource(currentProject.getProperties(), "Maven Project Properties");
        PropertiesConfigSource userPropertiesConfigSource = new PropertiesConfigSource(session.getUserProperties(), "Maven Session User Properties ");
        PropertiesConfigSource systemPropertiesConfigSource = new PropertiesConfigSource(session.getSystemProperties(), "Maven Session System Properties");
        return configurationProvider.getConfiguration(executionRoot, isEmpty(configurationLocations) ? empty() : of(configurationLocations),
            configurationBuilder.build(), projectConfigSource, settingsConfigSource, projectPropertiesConfigSource, userPropertiesConfigSource,
            systemPropertiesConfigSource);
    }

    /**
     * Apply store configuration.
     *
     * @param configurationBuilder
     *     The {@link ConfigurationBuilder}.
     */
    private void addStoreConfiguration(ConfigurationBuilder configurationBuilder) {
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
            com.buschmais.jqassistant.core.store.api.configuration.Store.URI, coalesce(storeUri, store.getUri()));
        configurationBuilder.with(Remote.class, Remote.USERNAME, coalesce(storeUserName, store.getUsername()));
        configurationBuilder.with(Remote.class, Remote.PASSWORD, coalesce(storePassword, store.getPassword()));
        configurationBuilder.with(Remote.class, Remote.ENCRYPTION, coalesce(storeEncryption, store.getEncryption()));
        configurationBuilder.with(Remote.class, Remote.TRUST_STRATEGY, coalesce(storeTrustStrategy, store.getTrustStrategy()));
        configurationBuilder.with(Remote.class, Remote.TRUST_CERTIFICATE, coalesce(storeTrustCertificate, store.getTrustCertificate()));
        configurationBuilder.with(Remote.class, Remote.PROPERTIES, store.getProperties());

        EmbeddedNeo4jConfiguration embedded = store.getEmbedded();
        configurationBuilder.with(Embedded.class, Embedded.CONNECTOR_ENABLED, coalesce(isConnectorRequired(), embedded.getConnectorEnabled())); //isConnectorRequired has precedence over the user setting
        configurationBuilder.with(Embedded.class, Embedded.LISTEN_ADDRESS, coalesce(embeddedListenAddress, embedded.getListenAddress()));
        configurationBuilder.with(Embedded.class, Embedded.BOLT_PORT, coalesce(embeddedBoltPort, embedded.getBoltPort()));
        configurationBuilder.with(Embedded.class, Embedded.HTTP_PORT, coalesce(embeddedHttpPort, embedded.getHttpPort()));
    }

    /**
     * Method to be overridden by sub-classes to add configuration properties.
     *
     * @param configurationBuilder
     *     The {@link ConfigurationBuilder}.
     */
    protected void configure(ConfigurationBuilder configurationBuilder) throws MojoExecutionException {
        configurationBuilder.with(MavenConfiguration.class, MavenConfiguration.SKIP, skip);
        configurationBuilder.with(Maven.class, Maven.USE_EXECUTION_ROOT_AS_PROJECT_ROOT, useExecutionRootAsProjectRoot);
        configurationBuilder.with(Rule.class, Rule.DIRECTORY, rulesDirectory);
    }

    /**
     * Retrieve the {@link PluginRepository}.
     *
     * @return the {@link PluginRepository}.
     */
    protected PluginRepository getPluginRepository(MavenConfiguration configuration) {
        return pluginRepositoryProvider.getPluginRepository(repositorySystem, repositorySystemSession, repositories, configuration.plugins());
    }

    /**
     * Defines an operation to execute on an initialized store instance.
     */
    protected interface StoreOperation {
        /**
         * Execute the operation.
         *
         * @param store
         *     The store.
         * @throws MojoExecutionException
         *     On execution errors.
         * @throws MojoFailureException
         *     On execution failures.
         */
        void run(Store store) throws MojoExecutionException, MojoFailureException;
    }

}
