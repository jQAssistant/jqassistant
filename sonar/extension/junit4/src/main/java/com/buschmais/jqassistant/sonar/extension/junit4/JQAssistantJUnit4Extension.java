package com.buschmais.jqassistant.sonar.extension.junit4;

import java.util.Collections;
import java.util.List;

import org.sonar.api.SonarPlugin;

/**
 * Defines the jQAssistant JUnit4 extension.
 */
public class JQAssistantJUnit4Extension extends SonarPlugin {

    @SuppressWarnings("rawtypes")
    @Override
    public List getExtensions() {
        return Collections.emptyList();
    }

}
