package com.buschmais.jqassistant.core.report;

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
 * A test language to be verified in the XML report.
 */
@Language()
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestLanguage {

    TestLanguageElement value();

    enum TestLanguageElement implements LanguageElement {
        TestElement {
            @Override
            public SourceProvider<? extends Descriptor> getSourceProvider() {
                return new SourceProvider<TestDescriptor>() {
                    @Override
                    public String getName(TestDescriptor descriptor) {
                        return descriptor.getValue();
                    }

                    @Override
                    public FileDescriptor getSourceFile(TestDescriptor descriptor) {
                        return new FileDescriptor() {
                            @Override
                            public String getFileName() {
                                return "Test.java";
                            }

                            @Override
                            public void setFileName(String fileName) {
                            }

                            @Override
                            public <I> I getId() {
                                return null;
                            }

                            @Override
                            public <T> T as(Class<T> type) {
                                return type.cast(this);
                            }

                            @Override
                            public <D> D getDelegate() {
                                return null;
                            }
                        };
                    }

                    @Override
                    public Integer getLineNumber(TestDescriptor descriptor) {
                        return 1;
                    }
                };
            }
        };

        @Override
        public String getLanguage() {
            return "TestLanguage";
        }
    }
}
