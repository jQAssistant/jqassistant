package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericTypeDeclarations;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;

public class TypeParameterIT extends AbstractJavaPluginIT {

    @Test
    void innerTypeParameterDeclaredByOuterType() throws RuleException {
        scanClasses(GenericTypeDeclarations.class, GenericTypeDeclarations.Inner.class);
        Result<Concept> conceptResult = applyConcept("java:InnerTypeParameterDeclaredByOuterType");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = conceptResult.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("OuterTypeDeclarations")).isEqualTo(1l);
        store.beginTransaction();
        List<TypeDescriptor> outerTypes = query(
                "MATCH (:Type{name:'GenericTypeDeclarations$Inner'})-[:REQUIRES_TYPE_PARAMETER]->(:TypeVariable)-[:DECLARED_BY]->(:TypeVariable)<-[:DECLARES_TYPE_PARAMETER]-(outer:Type) RETURN outer")
                        .getColumn("outer");
        assertThat(outerTypes).hasSize(1);
        assertThat(outerTypes.get(0)).is(matching(TypeDescriptorMatcher.typeDescriptor(GenericTypeDeclarations.class)));
        store.commitTransaction();
    }

    @Test
    void typeArgumentDeclaredByTypeParameter() throws RuleException {
        scanClasses(GenericTypeDeclarations.class, GenericTypeDeclarations.Inner.class);
        Result<Concept> conceptResult = applyConcept("java:TypeArgumentDeclaredByTypeParameter");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = conceptResult.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("TypeParameterDeclarations")).isEqualTo(2l);
        store.beginTransaction();
        List<String> typeVariables = query( //
                "MATCH (:Type{name:'GenericTypeDeclarations$Inner'})-[:EXTENDS_GENERIC]->(parameterizedType:ParameterizedType)," + //
                        "(:Type{name:'GenericTypeDeclarations'})-[dtp:DECLARES_TYPE_PARAMETER]->(typeVariable:TypeVariable)," + //
                        "(parameterizedType)-[:HAS_ACTUAL_TYPE_ARGUMENT]->(:TypeVariable)-[:DECLARED_BY]->(typeVariable:TypeVariable) " + //
                        "RETURN typeVariable.name as typeVariableName ORDER BY dtp.index").getColumn("typeVariableName");
        assertThat(typeVariables).hasSize(2);
        assertThat(typeVariables).containsExactly("X", "Y");
        store.commitTransaction();
    }
}
