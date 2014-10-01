package com.buschmais.jqassistant.plugin.common.api.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.jqassistant.core.report.api.Language;
import com.buschmais.jqassistant.core.report.api.LanguageElement;
import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.store.api.type.Descriptor;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

/**
 * Defines generic language elements.
 */
@Language
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Generic {

    GenericLanguageElement value();

    public enum GenericLanguageElement implements LanguageElement {
        Artifact {

            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<ArtifactDescriptor>() {
                    @Override
                    public String getName(ArtifactDescriptor descriptor) {
                        return descriptor.getFullQualifiedName();
                    }

                    @Override
                    public FileDescriptor getSourceFile(ArtifactDescriptor descriptor) {
                        return descriptor;
                    }

                    @Override
                    public Integer getLineNumber(ArtifactDescriptor descriptor) {
                        return null;
                    }
                };
            }
        };

        @Override
        public String getLanguage() {
            return "Generic";
        }
    }
}
