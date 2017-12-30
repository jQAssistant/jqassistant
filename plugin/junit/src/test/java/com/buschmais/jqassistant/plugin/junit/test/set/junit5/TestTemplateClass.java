package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class TestTemplateClass {
    @TestTemplate
    @ExtendWith(CustomInvocationContext.class)
    void templatedMethod(int value) {
        org.junit.jupiter.api.Assertions.assertTrue(() -> value > 0);
    }

    static class CustomInvocationContext implements TestTemplateInvocationContextProvider {
        @Override
        public boolean supportsTestTemplate(ExtensionContext context) {
            return true;
        }

        @Override
        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
            return Stream.of(invocationContext(1), invocationContext(2), invocationContext(3));
        }

        private TestTemplateInvocationContext invocationContext(int parameter) {
            return new TestTemplateInvocationContext() {
                @Override
                public String getDisplayName(int invocationIndex) {
                    return String.valueOf(parameter);
                }

                @Override
                public List<Extension> getAdditionalExtensions() {
                    return Collections.singletonList(new ParameterResolver() {
                        @Override
                        public boolean supportsParameter(ParameterContext parameterContext,
                                                         ExtensionContext extensionContext) {
                            return parameterContext.getParameter().getType().equals(int.class);
                        }

                        @Override
                        public Object resolveParameter(ParameterContext parameterContext,
                                                       ExtensionContext extensionContext) {
                            return parameter;
                        }
                    });
                }
            };
        }
    }
}
