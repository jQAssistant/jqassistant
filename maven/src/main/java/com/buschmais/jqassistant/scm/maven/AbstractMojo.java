package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.neo4j.embedded.configuration.Embedded;
import com.buschmais.jqassistant.scm.maven.configuration.Maven;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;
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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    public static final String STORE_DIRECTORY = "jqassistant/store";

    private static String createExecutionKey(MojoExecution mojoExecution) {
        // Do NOT use a custom class for execution keys, as different modules may use
        // different classloaders
        return mojoExecution.getGoal() + "@" + mojoExecution.getExecutionId();
    }

    /**
     * The config locations.
     */
    @Parameter(property = "jqassistant.configuration.locations")
    private List<String> configurationLocations;

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
        Store store = getStore(configuration, () -> new File(rootModule.getBuild()
            .getDirectory(), STORE_DIRECTORY));
        try {
            storeOperation.run(store);
        } finally {
            releaseStore(store, configuration.maven());
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
                "Cannot re-use store instance from reactor. Either declare the plugin as extension or execute Maven using the property -D" + Maven.REUSE_STORE
                    + "=false on the command line.");
        }
        return (Store) existingStore;
    }

    /**
     * Release a store instance.
     *
     * @param store
     *     The store instance.
     * @param maven
     */
    private void releaseStore(Store store, Maven maven) {
        if (!maven.reuseStore()) {
            cachingStoreProvider.closeStore(store);
        }
    }

    /**
     * Retrieve the runtime configuration.
     * <p>
     * The configuration directory is assumed to be located within the execution root of the Maven session.
     *
     * @return The {@link MavenConfiguration}.
     */
    private MavenConfiguration getConfiguration() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("MojoConfigSource", 110);
        if (isConnectorRequired()) {
            configurationBuilder.with(Embedded.class, Embedded.CONNECTOR_ENABLED, true);
        }
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
