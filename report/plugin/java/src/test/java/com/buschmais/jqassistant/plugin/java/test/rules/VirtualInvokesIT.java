package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.*;

import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Tests for the concept java:VirtualInvokes.
 */
class VirtualInvokesIT extends AbstractJavaPluginIT {

    @MethodSource("expectedVirtualInvocations")
    @ParameterizedTest
    void expectedVirtualInvokes(String clientMethodName, List<Matcher<? super MethodDescriptor>> methodDescriptorMatchers) throws RuleException {
        scanClasses(InterfaceType.class, AbstractClassType.class, SubClassType.class, ClientType.class, SiblingType.class);
        assertThat(applyConcept("java:VirtualInvokes").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        TestResult result = query(
            "MATCH (:Type{name:'ClientType'})-[:DECLARES]->(:Method{name:$clientMethodName})-[:VIRTUAL_INVOKES]->(invokedMethod:Method) WHERE invokedMethod.synthetic is null RETURN invokedMethod",
            Map.of("clientMethodName", clientMethodName));
        for (Map<String, Object> row : result.getRows()) {
            MethodDescriptor methodDescriptor = (MethodDescriptor) row.get("invokedMethod");
            assertThat(methodDescriptor, anyOf(methodDescriptorMatchers));
        }
        assertThat(result.getRows()
            .size()).isEqualTo(methodDescriptorMatchers.size());
        store.commitTransaction();
    }

    private static Stream<Arguments> expectedVirtualInvocations() throws NoSuchMethodException {
        return Stream.of( //
            of("<init>", emptyList()),  //
            of("methodOnInterfaceType", List.of(methodDescriptor(AbstractClassType.class, "method"), methodDescriptor(SubClassType.class, "method"),
                methodDescriptor(SiblingType.class, "method"))),  //
            of("methodOnAbstractClassType", List.of(methodDescriptor(AbstractClassType.class, "method"), methodDescriptor(SubClassType.class, "method"))),  //
            of("methodOnSubClassType", singletonList(methodDescriptor(SubClassType.class, "method"))), //

            of("abstractClassMethodOnInterfaceType",
                List.of(methodDescriptor(AbstractClassType.class, "abstractClassMethod"), methodDescriptor(SiblingType.class, "abstractClassMethod"))), //
            of("abstractClassMethodOnAbstractClassType", singletonList(methodDescriptor(AbstractClassType.class, "abstractClassMethod"))), //
            of("abstractClassMethodOnSubType", singletonList(methodDescriptor(AbstractClassType.class, "abstractClassMethod"))), //

            of("subClassMethodOnInterfaceType",
                List.of(methodDescriptor(SubClassType.class, "subClassMethod"), methodDescriptor(SiblingType.class, "subClassMethod"))), //
            of("subClassMethodOnAbstractClassType", singletonList(methodDescriptor(SubClassType.class, "subClassMethod"))), //
            of("subClassMethodOnSubType", singletonList(methodDescriptor(SubClassType.class, "subClassMethod"))), //

            of("genericMethodOnInterfaceType", List.of(methodDescriptor(SubClassType.class, "genericMethod", String.class),
                methodDescriptor(SiblingType.class, "genericMethod", Number.class))), //
            of("genericMethodOnSubClassType", singletonList(methodDescriptor(SubClassType.class, "genericMethod", String.class))));
    }
}
