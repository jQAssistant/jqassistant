package com.buschmais.jqassistant.core.report.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.Language;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.api.model.source.ArtifactLocation;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.xo.api.CompositeObject;

import static java.util.Optional.of;

/**
 * A test language to be verified in the XML report.
 */
@Language()
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestLanguage {

    TestLanguageElement value();

    enum TestLanguageElement implements LanguageElement {
        TestElement, DerivedTestElement;

        @Override
        public SourceProvider<? extends CompositeObject> getSourceProvider() {
            return new SourceProvider<TestDescriptorWithLanguageElement>() {
                @Override
                public String getName(TestDescriptorWithLanguageElement descriptor) {
                    return descriptor.getValue();
                }

                @Override
                public Optional<FileLocation> getSourceLocation(TestDescriptorWithLanguageElement descriptor) {
                    ArtifactLocation parent = ArtifactLocation.builder().fileName("test.jar").group(of("groupId")).name(of("artifactId")).type(of("jar"))
                            .classifier(of("jdk8")).version(of("1.0.0")).build();
                    return of(FileLocation.builder().fileName("Test.java").startLine(of(1)).endLine(of(2)).parent(of(parent)).build());
                }
            };
        }

        @Override
        public String getLanguage() {
            return "TestLanguage";
        }
    }
}
