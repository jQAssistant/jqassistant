package com.buschmais.jqassistant.sonar;

import com.buschmais.jqassistant.sonar.profile.JQAssistantProfileExporter;
import com.buschmais.jqassistant.sonar.rule.JQAssistantRuleRepository;
import com.buschmais.jqassistant.sonar.sensor.JQAssistantSensor;
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
