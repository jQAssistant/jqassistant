package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationSerializer;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all effective rules.
 */
@Mojo(name = "effective-configuration", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class EffectiveConfigurationMojo extends AbstractRuleMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectiveConfigurationMojo.class);

    private final ConfigurationSerializer<MavenConfiguration> configurationSerializer = new ConfigurationSerializer<>();

    @Override
    public void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        getLog().info("Effective configuration for '" + mojoExecutionContext.getRootModule()
            .getName() + "'\n" + configurationSerializer.toYaml(mojoExecutionContext.getConfiguration()));
    }

}
