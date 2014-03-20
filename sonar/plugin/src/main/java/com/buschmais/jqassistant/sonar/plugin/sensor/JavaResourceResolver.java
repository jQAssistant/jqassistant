package com.buschmais.jqassistant.sonar.plugin.sensor;

import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.JavaPackage;
import org.sonar.api.resources.Resource;

/**
 * Implementation of a {@link LanguageResourceResolver} for java elements.
 */
public class JavaResourceResolver implements LanguageResourceResolver {

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
