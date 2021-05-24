package com.buschmais.jqassistant.plugin.java.test.scanner.generics;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.UnboundClassTypeParameters;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaGenericsIT extends AbstractJavaPluginIT {

    @Test
    void unboundClassTypeParameters() {
        scanClasses(UnboundClassTypeParameters.class);
        store.beginTransaction();
        List<TypeVariableDescriptor> typeParameters = query(
                "MATCH (:Type:GenericDeclaration{name:'UnboundClassTypeParameters'})-[declares:DECLARES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:GenericType:TypeVariable) " + //
                        "RETURN typeParameter ORDER BY declares.index").getColumn("typeParameter");
        assertThat(typeParameters).hasSize(2);
        assertThat(typeParameters.get(0)).matches(x -> x.getName().equals("X"));
        assertThat(typeParameters.get(1)).matches(y -> y.getName().equals("Y"));
        store.commitTransaction();
    }

}
