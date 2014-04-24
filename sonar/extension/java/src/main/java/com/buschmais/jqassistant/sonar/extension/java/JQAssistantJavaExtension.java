package com.buschmais.jqassistant.sonar.extension.java;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.SonarPlugin;

/**
 * Defines the jQAssistant Java extension.
 */
public class JQAssistantJavaExtension extends SonarPlugin {

    @SuppressWarnings("rawtypes")
    @Override
    public List getExtensions() {
        return Arrays.asList(JavaResourceResolver.class);
    }

}
