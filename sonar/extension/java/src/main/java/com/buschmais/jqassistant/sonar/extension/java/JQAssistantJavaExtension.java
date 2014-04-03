package com.buschmais.jqassistant.sonar.extension.java;

import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Defines the jQAssistant Java extension.
 */
public class JQAssistantJavaExtension extends SonarPlugin {

    @Override
    public List getExtensions() {
        return Arrays.asList(JavaResourceResolver.class);
    }

}
