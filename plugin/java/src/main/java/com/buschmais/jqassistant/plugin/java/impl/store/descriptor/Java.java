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
@Language("Java")
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
                    public int[] getLineNumbers(PackageDescriptor descriptor) {
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
                        return null;
                    }

                    @Override
                    public int[] getLineNumbers(TypeDescriptor descriptor) {
                        return new int[] { 1 };
                    }
                };
            }
        },
        Field {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new MemberSourceDescriptor();
            }
        },
        ReadField {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<ReadsDescriptor>() {
                    @Override
                    public String getName(ReadsDescriptor descriptor) {
                        return new MemberSourceDescriptor().getName(descriptor.getMethod());
                    }

                    @Override
                    public String getSource(ReadsDescriptor descriptor) {
                        return descriptor.getMethod().getDeclaringType().getFileName();
                    }

                    @Override
                    public int[] getLineNumbers(ReadsDescriptor descriptor) {
                        return descriptor.getLineNumbers();
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
                        return new MemberSourceDescriptor().getName(descriptor.getMethod());
                    }

                    @Override
                    public String getSource(WritesDescriptor descriptor) {
                        return descriptor.getMethod().getDeclaringType().getFileName();
                    }

                    @Override
                    public int[] getLineNumbers(WritesDescriptor descriptor) {
                        return descriptor.getLineNumbers();
                    }
                };
            }
        },
        Method {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new MemberSourceDescriptor();
            }
        },
        Constructor {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new MemberSourceDescriptor();
            }
        },
        MethodInvocation {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<InvokesDescriptor>() {
                    @Override
                    public String getName(InvokesDescriptor descriptor) {
                        return new MemberSourceDescriptor().getName(descriptor.getInvokedMethod());
                    }

                    @Override
                    public String getSource(InvokesDescriptor descriptor) {
                        return descriptor.getInvokingMethod().getDeclaringType().getFileName();
                    }

                    @Override
                    public int[] getLineNumbers(InvokesDescriptor descriptor) {
                        return descriptor.getLineNumbers();
                    }
                };
            }
        };

        /**
         * {@link com.buschmais.jqassistant.core.report.api.SourceProvider}
         * implementation for type members.
         */
        private static class MemberSourceDescriptor implements SourceProvider<MemberDescriptor> {
            @Override
            public String getName(MemberDescriptor descriptor) {
                return descriptor.getDeclaringType().getFullQualifiedName() + "#" + descriptor.getSignature();
            }

            @Override
            public String getSource(MemberDescriptor descriptor) {
                return descriptor.getDeclaringType().getFileName();
            }

            @Override
            public int[] getLineNumbers(MemberDescriptor descriptor) {
                return null;
            }
        }
    }

}
