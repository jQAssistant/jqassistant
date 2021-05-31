package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.MemberDescriptor;
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
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Tests for the concept java:MemberInheritedFrom.
 */
public class MemberInheritedFromIT extends AbstractJavaPluginIT {

    @MethodSource("memberParameters")
    @ParameterizedTest
    public void inheritedFrom(Class<?> type, String signature, List<Matcher<? super MemberDescriptor>> memberDescriptorMatchers) throws RuleException {
        scanClasses(ClientType.class, InterfaceType.class, AbstractClassType.class, SubClassType.class);
        assertThat(applyConcept("java:MemberInheritedFrom").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query(
                "MATCH (type:Type{fqn:$type})-[:DECLARES]->(:Member{signature:$signature})-[:INHERITED_FROM]->(inheritedMember:Member) RETURN inheritedMember",
                MapBuilder.<String, Object> builder().entry("type", type.getName()).entry("signature", signature).build());
        assertThat(result.getRows().size(), equalTo(memberDescriptorMatchers.size()));
        for (Map<String, Object> row : result.getRows()) {
            MemberDescriptor memberDescriptor = (MemberDescriptor) row.get("inheritedMember");
            assertThat(memberDescriptor, anyOf(memberDescriptorMatchers));
        }
        store.commitTransaction();
    }

    private static Stream<Arguments> memberParameters() throws NoSuchMethodException, NoSuchFieldException {
        return Stream.of(of(InterfaceType.class, "void method()", emptyList()), of(AbstractClassType.class, "void method()", emptyList()),
                of(SubClassType.class, "void method()", emptyList()),

                of(InterfaceType.class, "void abstractClassMethod()", emptyList()), of(AbstractClassType.class, "void abstractClassMethod()", emptyList()),
                of(SubClassType.class, "void abstractClassMethod()", singletonList(methodDescriptor(AbstractClassType.class, "abstractClassMethod"))),

                of(InterfaceType.class, "void subClassMethod()", emptyList()),
                of(AbstractClassType.class, "void subClassMethod()", singletonList(methodDescriptor(InterfaceType.class, "subClassMethod"))),
                of(SubClassType.class, "void subClassMethod()", emptyList()),

                of(SubClassType.class, "int abstractClassField", singletonList(fieldDescriptor(AbstractClassType.class, "abstractClassField"))),
                of(SubClassType.class, "int overriddenAbstractClassField", emptyList()), of(SubClassType.class, "int subClassField", emptyList()));
    }

}
