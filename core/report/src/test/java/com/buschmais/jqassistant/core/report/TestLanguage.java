package com.buschmais.jqassistant.core.report;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.jqassistant.core.report.api.Language;
import com.buschmais.jqassistant.core.report.api.LanguageElement;
import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * A test language to be verified in the XML report.
 */
@Language("TestLanguage")
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
                    public String getSource(TestDescriptor descriptor) {
                        return "Test.java";
                    }

                    @Override
                    public int[] getLineNumbers(TestDescriptor descriptor) {
                        return new int[] { 1, 2 };
                    }
                };
            }
        };
    }
}
