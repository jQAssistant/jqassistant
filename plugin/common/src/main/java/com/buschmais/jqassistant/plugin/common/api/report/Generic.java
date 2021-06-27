package com.buschmais.jqassistant.plugin.common.api.report;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.Language;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;

/**
 * Defines generic language elements.
 */
@Language
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Generic {

    GenericLanguageElement value();

    enum GenericLanguageElement implements LanguageElement {

        /**
         * Named elements.
         */
        Named {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return (SourceProvider<NamedDescriptor>) descriptor -> descriptor.getName();
            }
        },

        /**
         * Artifacts.
         */
        ArtifactFile {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<ArtifactFileDescriptor>() {
                    @Override
                    public String getName(ArtifactFileDescriptor descriptor) {
                        return descriptor.getFullQualifiedName() != null ? descriptor.getFullQualifiedName() : descriptor.getFileName();
                    }

                    @Override
                    public String getSourceFile(ArtifactFileDescriptor descriptor) {
                        return descriptor.getFileName();
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
