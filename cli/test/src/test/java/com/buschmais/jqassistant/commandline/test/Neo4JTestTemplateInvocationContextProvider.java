package com.buschmais.jqassistant.commandline.test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.buschmais.jqassistant.commandline.test.AbstractCLIIT.NeoVersion;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class Neo4JTestTemplateInvocationContextProvider implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Stream.of(NeoVersion.values()).map(this::invocationContext);
    }

    private TestTemplateInvocationContext invocationContext(NeoVersion version) {
        return new TestTemplateInvocationContext() {

            @Override
            public String getDisplayName(int invocationIndex) {
                return version.toString();
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Collections.singletonList(new MyParameterResolver(version));
            }
        };
    }

    private static class MyParameterResolver implements ParameterResolver {
        private NeoVersion parameter;

        public MyParameterResolver(NeoVersion version) {
            this.parameter = version;
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType().equals(NeoVersion.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameter;
        }
    }
}
