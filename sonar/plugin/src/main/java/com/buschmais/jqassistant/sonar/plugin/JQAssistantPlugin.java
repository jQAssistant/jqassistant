package com.buschmais.jqassistant.sonar.plugin;

import static java.util.Arrays.asList;

import java.util.List;

import org.sonar.api.SonarPlugin;

import com.buschmais.jqassistant.sonar.plugin.language.JavaResourceResolver;
import com.buschmais.jqassistant.sonar.plugin.profile.JQAssistantProfileExporter;
import com.buschmais.jqassistant.sonar.plugin.rule.JQAssistantRuleRepository;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantSensor;

/**
 * Defines the jQAssistant plugin.
 */
public class JQAssistantPlugin extends SonarPlugin {

    /**
     * Return the plugin extensions.
     * 
     * @return The plugin extensions.
     */
    @SuppressWarnings("rawtypes")
    public List getExtensions() {
        return asList(JQAssistantRuleRepository.class, JQAssistantSensor.class, JQAssistantProfileExporter.class, JavaResourceResolver.class);
    }
}
