package com.buschmais.jqassistant.plugin.java.api.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.jqassistant.core.report.api.Language;
import com.buschmais.jqassistant.core.report.api.LanguageElement;
import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

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
                return new SourceProvider<PackageDirectoryDescriptor>() {
                    @Override
                    public String getName(PackageDirectoryDescriptor descriptor) {
                        return descriptor.getFullQualifiedName();
                    }

                    @Override
                    public FileDescriptor getSourceFile(PackageDirectoryDescriptor descriptor) {
                        return descriptor;
                    }

                    @Override
                    public Integer getLineNumber(PackageDirectoryDescriptor descriptor) {
                        return null;
                    }
                };
            }
        },
        Type {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<ClassFileDescriptor>() {
                    @Override
                    public String getName(ClassFileDescriptor descriptor) {
                        return descriptor.getFullQualifiedName();
                    }

                    @Override
                    public FileDescriptor getSourceFile(ClassFileDescriptor descriptor) {
                        return descriptor;
                    }

                    @Override
                    public Integer getLineNumber(ClassFileDescriptor descriptor) {
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
                        return new MemberSourceProvider().getName(descriptor.getMethod()) + ", line " + descriptor.getLineNumber();
                    }

                    @Override
                    public FileDescriptor getSourceFile(ReadsDescriptor descriptor) {
                        TypeDescriptor declaringType = descriptor.getMethod().getDeclaringType();
                        return declaringType instanceof ClassFileDescriptor ? (FileDescriptor) declaringType : null;
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
                        return new MemberSourceProvider().getName(descriptor.getMethod()) + ", line " + descriptor.getLineNumber();
                    }

                    @Override
                    public FileDescriptor getSourceFile(WritesDescriptor descriptor) {
                        TypeDescriptor declaringType = descriptor.getMethod().getDeclaringType();
                        return declaringType instanceof ClassFileDescriptor ? (FileDescriptor) declaringType : null;
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
                        return new MemberSourceProvider().getName(descriptor.getInvokingMethod()) + ", line " + descriptor.getLineNumber();
                    }

                    @Override
                    public FileDescriptor getSourceFile(InvokesDescriptor descriptor) {
                        TypeDescriptor declaringType = descriptor.getInvokingMethod().getDeclaringType();
                        return declaringType instanceof ClassFileDescriptor ? (FileDescriptor) declaringType : null;
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
            public FileDescriptor getSourceFile(MemberDescriptor descriptor) {
                TypeDescriptor declaringType = descriptor.getDeclaringType();
                return declaringType instanceof ClassFileDescriptor ? (FileDescriptor) declaringType : null;
            }

            @Override
            public Integer getLineNumber(MemberDescriptor descriptor) {
                return null;
            }
        }

        /**
         * Returns the file name of the given descriptor if it extends
         * {@link com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor}
         * .
         * 
         * @param descriptor
         *            The descriptor.
         * @return The
         */
        private static String getFileName(Descriptor descriptor) {
            return descriptor instanceof FileDescriptor ? ((FileDescriptor) descriptor).getFileName() : null;
        }
    }
}
