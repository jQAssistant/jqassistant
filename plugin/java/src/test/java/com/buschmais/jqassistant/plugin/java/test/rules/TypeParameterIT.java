package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericTypeDeclarations;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;

public class TypeParameterIT extends AbstractJavaPluginIT {

    @Test
    void outerClassTypeParameters() throws RuleException {
        scanClasses(GenericTypeDeclarations.class, GenericTypeDeclarations.Inner.class);
        applyConcept("java:InnerTypeParameterDeclaredByOuterType");
        store.beginTransaction();
        List<TypeDescriptor> outerTypes = query(
                "MATCH (:Type{name:'GenericTypeDeclarations$Inner'})-[:REQUIRES_TYPE_PARAMETER]->(:TypeVariable)-[:DECLARED_BY]->(:TypeVariable)<-[:DECLARES_TYPE_PARAMETER]-(outer:Type) RETURN outer")
                        .getColumn("outer");
        assertThat(outerTypes).hasSize(1);
        assertThat(outerTypes.get(0)).is(matching(TypeDescriptorMatcher.typeDescriptor(GenericTypeDeclarations.class)));
        store.commitTransaction();
    }
}
