package com.buschmais.jqassistant.scm.maven;

import java.util.List;
import java.util.Properties;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.shared.aether.AetherArtifactProvider;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;
import com.buschmais.jqassistant.scm.maven.provider.CachingStoreProvider;
import com.buschmais.jqassistant.scm.maven.provider.ConfigurationFileLoaderProvider;
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

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    /**
     * The config locations.
     */
    @Parameter(property = MavenConfigurationFactory.PROPERTY_CONFIGURATION_LOCATIONS)
    private List<String> configurationLocations;

    @Parameter
    private String yaml;

    @Parameter
    private Properties properties;

    /**
     * Skip the execution.
     */
    // property uses the same key as skip property in jQAssistant configuration
    @Parameter(property = Configuration.PREFIX + "." + Configuration.SKIP, defaultValue = "false")
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
     * The store repository.
     */
    @Component
    protected CachingStoreProvider cachingStoreProvider;

    /**
     * The Maven runtime information.
     */
    @Component
    private RuntimeInformation runtimeInformation;

    @Component
    private ConfigurationFileLoaderProvider configurationFileLoaderProvider;

    @Component
    private PluginRepositoryProvider pluginRepositoryProvider;

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

        MavenConfigurationFactory configurationFactory = new MavenConfigurationFactory(configurationFileLoaderProvider);
        MavenConfiguration mavenConfiguration = configurationFactory.getConfiguration(session, currentProject, configurationLocations, yaml, properties,
            isConnectorRequired());

        // Synchronize on this class as multiple instances of the plugin may exist in parallel builds
        synchronized (AbstractMojo.class) {
            AetherArtifactProvider artifactProvider = new AetherArtifactProvider(repositorySystem, repositorySystemSession, repositories);
            PluginRepository pluginRepository = pluginRepositoryProvider.getPluginRepository(mavenConfiguration, artifactProvider);
            MavenTaskContext mavenTaskContext = new MavenTaskContext(session, currentProject, execution, mavenConfiguration, pluginRepository, artifactProvider);
            if (skip) {
                // This is a shortcut to avoid loading the configuration if skip is given as part of the POM or system property.
                getLog().info("Skipping execution (required by plugin configuration");
                return;
            }
            getMavenTask().execute(mavenConfiguration, mavenTaskContext);
        }
    }

    protected abstract MavenTask getMavenTask();

    /**
     * Determines if the executed MOJO requires enabled connectors, can be overwritten by subclasses.
     *
     * @return <code>true</code> If connectors must be enabled.
     */
    protected boolean isConnectorRequired() {
        return false;
    }

}
