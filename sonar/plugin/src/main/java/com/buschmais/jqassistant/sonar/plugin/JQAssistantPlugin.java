package com.buschmais.jqassistant.sonar.plugin;

import com.buschmais.jqassistant.sonar.plugin.profile.JQAssistantProfileExporter;
import com.buschmais.jqassistant.sonar.plugin.rule.JQAssistantRuleRepository;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantSensor;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Defines the jQAssistant plugin.
 */
public class JQAssistantPlugin extends SonarPlugin {

    /**
     * Return the plugin extensions.
     *
     * @return The plugin extensions.
     */
    public List getExtensions() {
        return Arrays.asList(JQAssistantRuleRepository.class, JQAssistantSensor.class, JQAssistantProfileExporter.class);
    }
}
