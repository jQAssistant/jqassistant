package com.buschmais.jqassistant.sonar;

import com.buschmais.jqassistant.sonar.rule.JQAssistantRuleRepository;
import com.buschmais.jqassistant.sonar.sensor.JQAssistantSensor;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

public final class JQAssistantPlugin extends SonarPlugin {

    public List getExtensions() {
        return Arrays.asList(JQAssistantRuleRepository.class, JQAssistantSensor.class);
    }
}
