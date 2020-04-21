package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.AbstractClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.ClientType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.InterfaceType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.SubClassType;

import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Tests for the concept java:MethodOverrides.
 */
public class MethodOverridesIT extends AbstractJavaPluginIT {

    @MethodSource("parameters")
    @ParameterizedTest
    public void inheritedFrom(Class<?> type, String signature, List<Matcher<? super MethodDescriptor>> methodDescriptorMatchers) throws RuleException {
        scanClasses(ClientType.class, InterfaceType.class, AbstractClassType.class, SubClassType.class);
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query(
                "MATCH (type:Type{fqn:$type})-[:DECLARES]->(:Method{signature:$signature})-[:OVERRIDES]->(overriddenMethod:Method) RETURN overriddenMethod",
                MapBuilder.<String, Object> builder().entry("type", type.getName()).entry("signature", signature).build());
        assertThat(result.getRows().size(), equalTo(methodDescriptorMatchers.size()));
        for (Map<String, Object> row : result.getRows()) {
            MethodDescriptor methodDescriptor = (MethodDescriptor) row.get("overriddenMethod");
            assertThat(methodDescriptor, anyOf(methodDescriptorMatchers));
        }
        store.commitTransaction();
    }

    private static Stream<Arguments> parameters() throws NoSuchMethodException {
        return Stream.of(of(InterfaceType.class, "void method()", emptyList()),
                of(AbstractClassType.class, "void method()", singletonList(methodDescriptor(InterfaceType.class, "method"))),
                of(SubClassType.class, "void method()", singletonList(methodDescriptor(AbstractClassType.class, "method"))),

                of(InterfaceType.class, "void abstractClassMethod()", emptyList()),
                of(AbstractClassType.class, "void abstractClassMethod()", singletonList(methodDescriptor(InterfaceType.class, "abstractClassMethod"))),
                of(SubClassType.class, "void abstractClassMethod()", emptyList()),

                of(InterfaceType.class, "void subClassMethod()", emptyList()),
                of(AbstractClassType.class, "void subClassMethod()", emptyList()),
                of(SubClassType.class, "void subClassMethod()", singletonList(methodDescriptor(InterfaceType.class, "subClassMethod"))));
    }
}
