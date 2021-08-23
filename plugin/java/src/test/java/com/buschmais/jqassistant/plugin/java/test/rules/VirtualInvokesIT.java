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
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Tests for the concept java:VirtualInvokes.
 */
class VirtualInvokesIT extends AbstractJavaPluginIT {

    @MethodSource("parameters")
    @ParameterizedTest
    void virtualInvokes(String clientMethodName, List<Matcher<? super MethodDescriptor>> methodDescriptorMatchers) throws RuleException {
        scanClasses(InterfaceType.class, AbstractClassType.class, SubClassType.class, ClientType.class);
        assertThat(applyConcept("java:VirtualInvokes").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query(
                "MATCH (:Type{name:'ClientType'})-[:DECLARES]->(:Method{name:$clientMethodName})-[:VIRTUAL_INVOKES]->(invokedMethod:Method) RETURN invokedMethod",
                MapBuilder.<String, Object> builder().entry("clientMethodName", clientMethodName).build());
        assertThat(result.getRows().size(), equalTo(methodDescriptorMatchers.size()));
        for (Map<String, Object> row : result.getRows()) {
            MethodDescriptor methodDescriptor = (MethodDescriptor) row.get("invokedMethod");
            assertThat(methodDescriptor, anyOf(methodDescriptorMatchers));
        }
        store.commitTransaction();
    }

    private static Stream<Arguments> parameters() throws NoSuchMethodException {
        return Stream.of(
                of("methodOnInterfaceType", asList(methodDescriptor(AbstractClassType.class, "method"), methodDescriptor(SubClassType.class, "method"))),
                of("methodOnAbstractClassType", asList(methodDescriptor(AbstractClassType.class, "method"), methodDescriptor(SubClassType.class, "method"))),
                of("methodOnSubClassType", singletonList(methodDescriptor(SubClassType.class, "method"))),

                of("abstractClassMethodOnInterfaceType", singletonList(methodDescriptor(AbstractClassType.class, "abstractClassMethod"))),
                of("abstractClassMethodOnAbstractClassType", singletonList(methodDescriptor(AbstractClassType.class, "abstractClassMethod"))),
                of("abstractClassMethodOnSubType", singletonList(methodDescriptor(AbstractClassType.class, "abstractClassMethod"))),

                of("subClassMethodOnInterfaceType", singletonList(methodDescriptor(SubClassType.class, "subClassMethod"))),
                of("subClassMethodOnAbstractClassType", singletonList(methodDescriptor(SubClassType.class, "subClassMethod"))),
                of("subClassMethodOnSubType", singletonList(methodDescriptor(SubClassType.class, "subClassMethod"))));
    }
}
