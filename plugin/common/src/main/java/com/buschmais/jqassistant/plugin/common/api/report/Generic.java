package com.buschmais.jqassistant.plugin.common.api.report;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.Language;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;

import static java.util.Optional.empty;

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
            public SourceProvider<NamedDescriptor> getSourceProvider() {
                return new SourceProvider<NamedDescriptor>() {
                    @Override
                    public String getName(NamedDescriptor descriptor) {
                        return descriptor.getName();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(NamedDescriptor descriptor) {
                        if (descriptor instanceof FileDescriptor) {
                            return FileSourceHelper.getSourceLocation((FileDescriptor) descriptor, empty(), empty());
                        }
                        return empty();
                    }
                };
            }
        },

        /**
         * Files.
         */
        File {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<FileDescriptor>() {

                    @Override
                    public String getName(FileDescriptor descriptor) {
                        return descriptor.getFileName();
                    }

                    @Override
                    public String getSourceFile(FileDescriptor descriptor) {
                        return descriptor.getFileName();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(FileDescriptor descriptor) {
                        return FileSourceHelper.getSourceLocation(descriptor, empty(), empty());
                    }
                };
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

                    @Override
                    public Optional<FileLocation> getSourceLocation(ArtifactFileDescriptor descriptor) {
                        return FileSourceHelper.getSourceLocation(descriptor, empty(), empty());
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
