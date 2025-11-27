package com.buschmais.jqassistant.plugin.maven3.api.report;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.Language;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.common.api.report.FileSourceHelper;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;

import static java.util.Optional.empty;

/**
 * Defines language elements for Maven.
 */
@Language
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Maven {

    MavenLanguageElement value();

    enum MavenLanguageElement implements LanguageElement {

        Pom {
            @Override
            public SourceProvider<MavenPomDescriptor> getSourceProvider() {
                return new SourceProvider<>() {
                    @Override
                    public String getName(MavenPomDescriptor descriptor) {
                        String groupId = descriptor.getGroupId() != null ?
                                descriptor.getGroupId() :
                                descriptor.getParent()
                                        .getGroup();
                        return groupId + ":" + descriptor.getArtifactId();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(MavenPomDescriptor descriptor) {
                        return empty();
                    }
                };
            }
        },

        PomXmlFile {
            @Override
            public SourceProvider<MavenPomXmlDescriptor> getSourceProvider() {
                return new SourceProvider<>() {
                    @Override
                    public String getName(MavenPomXmlDescriptor descriptor) {
                        return Pom.getSourceProvider()
                                .getName(descriptor);
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(MavenPomXmlDescriptor descriptor) {
                        return FileSourceHelper.getSourceLocation(descriptor, empty(), empty());
                    }
                };
            }
        };

        @Override
        public String getLanguage() {
            return "Maven";
        }

    }

}
