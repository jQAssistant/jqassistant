package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.shared.configuration.ConfigurationSerializer;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Prints the current configuration on the console.
 */
@Mojo(name = "effective-configuration", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class EffectiveConfigurationMojo extends AbstractRuleMojo {

    private final com.buschmais.jqassistant.core.shared.configuration.ConfigurationSerializer<MavenConfiguration> configurationSerializer = new ConfigurationSerializer<>();

    @Override
    protected void beforeProject(MojoExecutionContext mojoExecutionContext) {
        // nothing to do here
    }

    @Override
    public void afterProject(MojoExecutionContext mojoExecutionContext) {
        getLog().info("Effective configuration for '" + mojoExecutionContext.getRootModule()
            .getName() + "'\n" + configurationSerializer.toYaml(mojoExecutionContext.getConfiguration()));
    }

}
