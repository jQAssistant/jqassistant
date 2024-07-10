package com.buschmais.jqassistant.plugin.java.api.report;

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
import com.buschmais.jqassistant.plugin.common.api.report.FileSourceHelper;
import com.buschmais.jqassistant.plugin.java.api.model.*;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Defines the language elements for "Java".
 */
@Language
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Java {

    JavaLanguageElement value();

    enum JavaLanguageElement implements LanguageElement {
        Package {
            @Override
            public SourceProvider<PackageDescriptor> getSourceProvider() {
                return new SourceProvider<PackageDescriptor>() {
                    @Override
                    public String getName(PackageDescriptor descriptor) {
                        return descriptor.getFullQualifiedName();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(PackageDescriptor descriptor) {
                        return FileSourceHelper.getSourceLocation(descriptor, empty(), empty());
                    }
                };
            }
        },
        Type {
            @Override
            public SourceProvider<ClassFileDescriptor> getSourceProvider() {
                return new SourceProvider<ClassFileDescriptor>() {
                    @Override
                    public String getName(ClassFileDescriptor descriptor) {
                        return descriptor.getFullQualifiedName();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(ClassFileDescriptor descriptor) {
                        return TypeSourceHelper.getSourceLocation(descriptor);
                    }
                };
            }
        },
        Field {
            @Override
            public SourceProvider<FieldDescriptor> getSourceProvider() {
                return new FieldSourceProvider();
            }
        },
        Variable {
            @Override
            public SourceProvider<VariableDescriptor> getSourceProvider() {
                return new SourceProvider<VariableDescriptor>() {

                    @Override
                    public String getName(VariableDescriptor descriptor) {
                        return descriptor.getMethod().getSignature() + "#" + descriptor.getSignature();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(VariableDescriptor descriptor) {
                        return TypeSourceHelper.getSourceLocation(descriptor.getMethod().getDeclaringType());
                    }

                };
            }
        },
        ReadField {
            @Override
            public SourceProvider<ReadsDescriptor> getSourceProvider() {
                return new SourceProvider<ReadsDescriptor>() {
                    @Override
                    public String getName(ReadsDescriptor descriptor) {
                        return descriptor.getMethod().getSignature() + ", line " + descriptor.getLineNumber();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(ReadsDescriptor descriptor) {
                        return TypeSourceHelper.getSourceLocation(descriptor.getMethod().getDeclaringType(), descriptor.getLineNumber());
                    }
                };
            }
        },
        WriteField {
            @Override
            public SourceProvider<WritesDescriptor> getSourceProvider() {
                return new SourceProvider<WritesDescriptor>() {
                    @Override
                    public String getName(WritesDescriptor descriptor) {
                        return descriptor.getMethod().getSignature() + ", line " + descriptor.getLineNumber();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(WritesDescriptor descriptor) {
                        return TypeSourceHelper.getSourceLocation(descriptor.getMethod().getDeclaringType(), descriptor.getLineNumber());
                    }

                };
            }
        },
        Method {
            @Override
            public SourceProvider<MethodDescriptor> getSourceProvider() {
                return new MethodSourceProvider();
            }
        },
        Constructor {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new MethodSourceProvider();
            }
        },
        MethodInvocation {
            @Override
            public SourceProvider<InvokesDescriptor> getSourceProvider() {
                return new SourceProvider<InvokesDescriptor>() {
                    @Override
                    public String getName(InvokesDescriptor descriptor) {
                        return descriptor.getInvokingMethod().getSignature() + ", line " + descriptor.getLineNumber();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(InvokesDescriptor descriptor) {
                        return TypeSourceHelper.getSourceLocation(descriptor.getInvokingMethod().getDeclaringType(), descriptor.getLineNumber());
                    }
                };
            }
        },
        TypeDepdendency {
            @Override
            public SourceProvider<TypeDependsOnDescriptor> getSourceProvider() {
                return new SourceProvider<TypeDependsOnDescriptor>() {
                    @Override
                    public String getName(TypeDependsOnDescriptor descriptor) {
                        return descriptor.getDependent().getName() + "->" + descriptor.getDependency().getName();
                    }

                    @Override
                    public Optional<FileLocation> getSourceLocation(TypeDependsOnDescriptor descriptor) {
                        return TypeSourceHelper.getSourceLocation(descriptor.getDependent());
                    }
                };
            }
        };

        @Override
        public String getLanguage() {
            return "Java";
        }

        /**
         * {@link SourceProvider} implementation for type members.
         */
        private abstract static class MemberSourceProvider<D extends MemberDescriptor> implements SourceProvider<D> {
            @Override
            public String getName(D descriptor) {
                return descriptor.getSignature();
            }

        }

        /**
         * {@link SourceProvider} implementation for {@link FieldDescriptor}s.
         */
        private static class FieldSourceProvider extends MemberSourceProvider<FieldDescriptor> {

            @Override
            public Optional<FileLocation> getSourceLocation(FieldDescriptor descriptor) {
                return TypeSourceHelper.getSourceLocation(descriptor.getDeclaringType());
            }

        }

        /**
         * {@link SourceProvider} implementation for {@link MethodDescriptor}s.
         */
        private static class MethodSourceProvider extends MemberSourceProvider<MethodDescriptor> {

            @Override
            public Optional<FileLocation> getSourceLocation(MethodDescriptor descriptor) {
                return TypeSourceHelper.getSourceLocation(descriptor.getDeclaringType(), ofNullable(descriptor.getFirstLineNumber()),
                        ofNullable(descriptor.getLastLineNumber()));
            }

        }
    }
}
