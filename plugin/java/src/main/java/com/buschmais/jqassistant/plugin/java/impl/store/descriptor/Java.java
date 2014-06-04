package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.jqassistant.core.report.api.Language;
import com.buschmais.jqassistant.core.report.api.LanguageElement;
import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * Defines the language elements for "Java".
 */
@Language
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Java {

    JavaLanguageElement value();

    public enum JavaLanguageElement implements LanguageElement {
        Package {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<PackageDescriptor>() {
                    @Override
                    public String getName(PackageDescriptor descriptor) {
                        return descriptor.getFullQualifiedName();
                    }

                    @Override
                    public String getSource(PackageDescriptor descriptor) {
                        return descriptor.getFileName();
                    }

                    @Override
                    public Integer getLineNumber(PackageDescriptor descriptor) {
                        return null;
                    }
                };
            }
        },
        Type {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<TypeDescriptor>() {
                    @Override
                    public String getName(TypeDescriptor descriptor) {
                        return descriptor.getFullQualifiedName();
                    }

                    @Override
                    public String getSource(TypeDescriptor descriptor) {
                        return descriptor.getFileName();
                    }

                    @Override
                    public Integer getLineNumber(TypeDescriptor descriptor) {
                        return null;
                    }
                };
            }
        },
        Field {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new MemberSourceProvider();
            }
        },
        ReadField {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<ReadsDescriptor>() {
                    @Override
                    public String getName(ReadsDescriptor descriptor) {
                        return new MemberSourceProvider().getName(descriptor.getMethod());
                    }

                    @Override
                    public String getSource(ReadsDescriptor descriptor) {
                        return descriptor.getMethod().getDeclaringType().getFileName();
                    }

                    @Override
                    public Integer getLineNumber(ReadsDescriptor descriptor) {
                        return descriptor.getLineNumber();
                    }
                };
            }
        },
        WriteField {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<WritesDescriptor>() {
                    @Override
                    public String getName(WritesDescriptor descriptor) {
                        return new MemberSourceProvider().getName(descriptor.getMethod());
                    }

                    @Override
                    public String getSource(WritesDescriptor descriptor) {
                        return descriptor.getMethod().getDeclaringType().getFileName();
                    }

                    @Override
                    public Integer getLineNumber(WritesDescriptor descriptor) {
                        return descriptor.getLineNumber();
                    }
                };
            }
        },
        Method {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new MemberSourceProvider();
            }
        },
        Constructor {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new MemberSourceProvider();
            }
        },
        MethodInvocation {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<InvokesDescriptor>() {
                    @Override
                    public String getName(InvokesDescriptor descriptor) {
                        return new MemberSourceProvider().getName(descriptor.getInvokedMethod());
                    }

                    @Override
                    public String getSource(InvokesDescriptor descriptor) {
                        return descriptor.getInvokingMethod().getDeclaringType().getFileName();
                    }

                    @Override
                    public Integer getLineNumber(InvokesDescriptor descriptor) {
                        return descriptor.getLineNumber();
                    }
                };
            }
        };

        @Override
        public String getLanguage() {
            return "Java";
        }

        /**
         * {@link com.buschmais.jqassistant.core.report.api.SourceProvider}
         * implementation for type members.
         */
        private static class MemberSourceProvider implements SourceProvider<MemberDescriptor> {
            @Override
            public String getName(MemberDescriptor descriptor) {
                return descriptor.getDeclaringType().getFullQualifiedName() + "#" + descriptor.getSignature();
            }

            @Override
            public String getSource(MemberDescriptor descriptor) {
                return descriptor.getDeclaringType().getFileName();
            }

            @Override
            public Integer getLineNumber(MemberDescriptor descriptor) {
                return null;
            }
        }
    }

}
