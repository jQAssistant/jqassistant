package com.buschmais.jqassistant.sonar.extension.java;

import org.sonar.api.BatchExtension;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.JavaPackage;
import org.sonar.api.resources.Resource;

import com.buschmais.jqassistant.sonar.plugin.sensor.LanguageResourceResolver;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.sonar.plugin.sensor.LanguageResourceResolver}
 * for java elements.
 */
public class JavaResourceResolver implements LanguageResourceResolver, BatchExtension {

    @Override
    public String getLanguage() {
        return "Java";
    }

    @Override
    public Resource<?> resolve(String type, String name) {
        if ("Type".equals(type)) {
            return new JavaFile(name);
        } else if ("Package".equals(type)) {
            return new JavaPackage(name);
        }
        return null;
    }

}
