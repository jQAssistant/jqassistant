package com.buschmais.jqassistant.sonar.plugin.language;

import org.sonar.api.BatchExtension;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.java.Java;

import com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement;
import com.buschmais.jqassistant.sonar.plugin.sensor.LanguageResourceResolver;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.sonar.plugin.sensor.LanguageResourceResolver}
 * for java elements.
 */
public class JavaResourceResolver implements LanguageResourceResolver, BatchExtension {

    @Override
    public String getLanguage() {
        return Java.KEY;
    }

    @Override
    public Resource resolve(String type, String name) {
    	//FIXME: The replacement of 'JavaFile' by 'File' could not work...
        if (JavaLanguageElement.Type.name().equals(type)) {
            return new File(name);
        } else if (JavaLanguageElement.Field.name().equals(type) || JavaLanguageElement.MethodInvocation.name().equals(type)
                || JavaLanguageElement.ReadField.name().equals(type) || JavaLanguageElement.WriteField.name().equals(type)
                || JavaLanguageElement.MethodInvocation.name().equals(type)) {
            return new File(name.split("#")[0]);
        } else if (JavaLanguageElement.Package.name().equals(type)) {
            return new Directory(name);
        }
        return null;
    }

}
