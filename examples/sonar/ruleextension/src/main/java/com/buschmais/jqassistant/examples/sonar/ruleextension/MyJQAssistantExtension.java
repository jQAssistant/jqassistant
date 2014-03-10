package com.buschmais.jqassistant.examples.sonar.ruleextension;

import org.sonar.api.SonarPlugin;

import java.util.Collections;
import java.util.List;

/**
 * Defines a sonar extension.
 */
public class MyJQAssistantExtension extends SonarPlugin {

    @Override
    public List getExtensions() {
        return Collections.emptyList();
    }

}
