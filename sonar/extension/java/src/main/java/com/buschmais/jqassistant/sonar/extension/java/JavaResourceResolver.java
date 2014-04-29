package com.buschmais.jqassistant.sonar.extension.java;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement;

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
        if (JavaLanguageElement.Type.name().equals(type)) {
            return new JavaFile(name);
        } else if (JavaLanguageElement.Field.name().equals(type) || JavaLanguageElement.MethodInvocation.name().equals(type)
                || JavaLanguageElement.ReadField.name().equals(type) || JavaLanguageElement.WriteField.name().equals(type)
                || JavaLanguageElement.MethodInvocation.name().equals(type)) {
            return new JavaFile(name.split("#")[0]);
        } else if (JavaLanguageElement.Package.name().equals(type)) {
            return new JavaPackage(name);
        }
        return null;
    }

}
