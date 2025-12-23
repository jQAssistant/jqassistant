package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.shared.configuration.ConfigurationSerializer;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Prints the current configuration on the console.
 */
@Mojo(name = "effective-configuration", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
@Slf4j
public class EffectiveConfigurationMojo extends AbstractMojo {

    private final com.buschmais.jqassistant.core.shared.configuration.ConfigurationSerializer<MavenConfiguration> configurationSerializer = new ConfigurationSerializer<>();

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenTask() {

            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) {
                log.info("Effective configuration for '{}'\n{}", mavenTaskContext.getRootModule()
                    .getName(), configurationSerializer.toYaml(mavenTaskContext.getConfiguration()));
            }

        };
    }

}
