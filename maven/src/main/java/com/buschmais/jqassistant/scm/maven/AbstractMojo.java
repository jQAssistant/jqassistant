package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.report.api.BuildConfigBuilder;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.shared.aether.AetherArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;
import com.buschmais.jqassistant.scm.maven.configuration.Maven;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;
import com.buschmais.jqassistant.scm.maven.configuration.source.EmptyConfigSource;
import com.buschmais.jqassistant.scm.maven.configuration.source.MavenProjectConfigSource;
import com.buschmais.jqassistant.scm.maven.configuration.source.MavenPropertiesConfigSource;
import com.buschmais.jqassistant.scm.maven.configuration.source.SettingsConfigSource;
import com.buschmais.jqassistant.scm.maven.provider.CachingStoreProvider;
import com.buschmais.jqassistant.scm.maven.provider.PluginRepositoryProvider;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.source.yaml.YamlConfigSource;
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
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.lang.Thread.currentThread;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    public static final String STORE_DIRECTORY = "jqassistant/store";

    private static final int CONFIGURATION_ORDINAL_EXECUTION_ROOT = 100;

    private static final String PROPERTY_CONFIGURATION_LOCATIONS = "jqassistant.configuration.locations";

    private static String createExecutionKey(MojoExecution mojoExecution) {
        // Do NOT use a custom class for execution keys, as different modules may use
        // different classloaders
        return mojoExecution.getGoal() + "@" + mojoExecution.getExecutionId();
    }

    /**
     * The config locations.
     */
    @Parameter(property = PROPERTY_CONFIGURATION_LOCATIONS)
    private List<String> configurationLocations;

    @Parameter
    private String yaml;

    @Parameter
    private Properties properties;

    @Parameter(property = "jqassistant.skip", defaultValue = "false")
    private boolean skip;

    /**
     * The Maven Session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * The Maven project.
     */
    @Parameter(property = "project")
    private MavenProject currentProject;

    /**
     * The current execution.
     */
    @Parameter(property = "mojoExecution")
    private MojoExecution execution;

    /**
     * The Maven runtime information.
     */
    @Component
    private RuntimeInformation runtimeInformation;

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
        if (!runtimeInformation.isMavenVersion("[3.6.3,)")) {
            throw new MojoExecutionException("jQAssistant requires Maven 3.6.3 or above.");
        }
        // Synchronize on this class as multiple instances of the plugin may exist in parallel builds
        synchronized (AbstractMojo.class) {
            if(skip) {
                getLog().info("Skipping execution.");
                return;
            }
            MavenConfiguration configuration = getConfiguration();
            if (configuration.skip()) {
                getLog().info("Skipping execution.");
            } else {
                AetherArtifactProvider artifactResolver = new AetherArtifactProvider(repositorySystem, repositorySystemSession, repositories);
                PluginRepository pluginRepository = pluginRepositoryProvider.getPluginRepository(configuration, artifactResolver);
                MojoExecutionContext mojoExecutionContext = new MojoExecutionContext(session, currentProject, execution, configuration, pluginRepository);
                MavenProject rootModule = mojoExecutionContext.getRootModule();
                Set<MavenProject> executedModules = getExecutedModules(rootModule);
                ClassLoader contextClassLoader = currentThread().getContextClassLoader();
                currentThread().setContextClassLoader(pluginRepository.getClassLoader());
                try {
                    if (isResetStoreBeforeExecution(configuration) && executedModules.isEmpty()) {
                        withStore(Store::reset, mojoExecutionContext);
                    }
                    execute(mojoExecutionContext, executedModules);
                } finally {
                    currentThread().setContextClassLoader(contextClassLoader);
                }
                executedModules.add(currentProject);
            }
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
     * Determine if the store shall be reset before execution of the mojo,can be overwritten by subclasses.
     *
     * @return `true` if the store shall be reset.
     */
    protected boolean isResetStoreBeforeExecution(MavenConfiguration configuration) {
        return false;
    }

    /**
     * Determines if the executed MOJO requires enabled connectors, can be overwritten by subclasses.
     *
     * @return <code>true</code> If connectors must be enabled.
     */
    protected boolean isConnectorRequired() {
        return false;
    }

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
    protected final void withStore(StoreOperation storeOperation, MojoExecutionContext mojoExecutionContext)
        throws MojoExecutionException, MojoFailureException {
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        MavenConfiguration configuration = mojoExecutionContext.getConfiguration();
        Store store = getStore(mojoExecutionContext, () -> new File(rootModule.getBuild()
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
    private Store getStore(MojoExecutionContext mojoExecutionContext, Supplier<File> storeDirectorySupplier) throws MojoExecutionException {
        MavenConfiguration configuration = mojoExecutionContext.getConfiguration();
        Object existingStore = cachingStoreProvider.getStore(configuration.store(), storeDirectorySupplier, mojoExecutionContext.getPluginRepository(),
            new AetherArtifactProvider(repositorySystem, repositorySystemSession, repositories));
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
        ConfigSource buildConfigSource = BuildConfigBuilder.getConfigSource(session.getTopLevelProject()
            .getName(), session.getStartTime()
            .toInstant()
            .atZone(ZoneId.systemDefault()));
        ConfigurationBuilder mojoConfigurationBuilder = new ConfigurationBuilder("MojoConfigSource", 110);
        // activate connector (depending on goal)
        if (isConnectorRequired()) {
            mojoConfigurationBuilder.with(Embedded.class, Embedded.CONNECTOR_ENABLED, true);
        }
        ConfigSource mojoConfigSource = mojoConfigurationBuilder.build();

        MavenProjectConfigSource projectConfigSource = new MavenProjectConfigSource(currentProject);
        SettingsConfigSource settingsConfigSource = new SettingsConfigSource(session.getSettings());
        MavenPropertiesConfigSource projectPropertiesConfigSource = new MavenPropertiesConfigSource(currentProject.getProperties(), "Maven Project Properties");
        MavenPropertiesConfigSource userPropertiesConfigSource = new MavenPropertiesConfigSource(session.getUserProperties(), "Maven Session User Properties ");
        MavenPropertiesConfigSource systemPropertiesConfigSource = new MavenPropertiesConfigSource(session.getSystemProperties(),
            "Maven Session System Properties");
        ConfigSource yamlConfiguration = getYamlPluginConfiguration();
        ConfigSource propertiesConfiguration = getPropertiesPluginConfiguration();

        ConfigSource[] configSources = new ConfigSource[] { buildConfigSource, mojoConfigSource, projectConfigSource, settingsConfigSource,
            projectPropertiesConfigSource, userPropertiesConfigSource, systemPropertiesConfigSource, yamlConfiguration, propertiesConfiguration };
        File userHome = new File(System.getProperty("user.home"));
        File executionRootDirectory = new File(session.getExecutionRootDirectory());
        ConfigurationMappingLoader.Builder<MavenConfiguration> builder = ConfigurationMappingLoader.builder(
                MavenConfiguration.class, configurationLocations)
            .withUserHome(userHome)
            .withDirectory(executionRootDirectory, CONFIGURATION_ORDINAL_EXECUTION_ROOT)
            .withEnvVariables()
            .withClasspath()
            .withProfiles(session.getProjectBuildingRequest()
                .getActiveProfileIds())
            .withIgnoreProperties(Set.of(PROPERTY_CONFIGURATION_LOCATIONS));
        if (!executionRootDirectory.equals(currentProject.getBasedir())) {
            builder.withWorkingDirectory(currentProject.getBasedir());
        }
        return builder.load(configSources);
    }

    private ConfigSource getYamlPluginConfiguration() {
        return isNotEmpty(yaml) ?
            new YamlConfigSource("Maven plugin execution YAML configuration", yaml, MavenPropertiesConfigSource.CONFIGURATION_ORDINAL_MAVEN_PROPERTIES) :
            EmptyConfigSource.INSTANCE;
    }

    private ConfigSource getPropertiesPluginConfiguration() {
        return properties != null ?
            new PropertiesConfigSource(properties, "Maven plugin execution properties configuration",
                MavenPropertiesConfigSource.CONFIGURATION_ORDINAL_MAVEN_PROPERTIES) :
            EmptyConfigSource.INSTANCE;
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
