package com.buschmais.jqassistant.examples.sonar.ruleextension;

import java.util.Collections;
import java.util.List;

import org.sonar.api.SonarPlugin;

/**
 * Defines a sonar extension.
 */
public class MyJQAssistantExtension extends SonarPlugin {

    @SuppressWarnings("rawtypes")
    @Override
    public List getExtensions() {
        return Collections.emptyList();
    }

}
