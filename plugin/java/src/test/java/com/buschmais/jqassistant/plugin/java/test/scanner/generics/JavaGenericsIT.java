package com.buschmais.jqassistant.plugin.java.test.scanner.generics;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.UnboundClassTypeParameters;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaGenericsIT extends AbstractJavaPluginIT {

    @Test
    void unboundClassTypeParameters() {
        scanClasses(UnboundClassTypeParameters.class);
        store.beginTransaction();
        List<TypeParameterDescriptor> typeParameters = query(
                "MATCH (:Type{name:'Test2'})-[:DECLARES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:TypeParameter) RETURN typeParameter ORDER BY typeParameter.index")
                        .getColumn("typeParameter");
        assertThat(typeParameters).hasSize(2);
        assertThat(typeParameters.get(0)).matches(x -> x.getName().equals("X") && x.getIndex() == 0);
        assertThat(typeParameters.get(1)).matches(y -> y.getName().equals("Y") && y.getIndex() == 1);
        store.commitTransaction();
    }

}
