package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.java.api.model.ConstructorDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.enumeration.EnumerationType;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.FieldDescriptorCondition.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains test which verify correct scanning of constructors.
 */
class EnumerationIT extends AbstractJavaPluginIT {

    /**
     * Verifies scanning of {@link EnumerationType}.
     *
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    void implicitDefaultConstructor() throws ReflectiveOperationException {
        scanClasses(EnumerationType.class);
        store.beginTransaction();
        assertThat(query("MATCH (e:Type:Enum) RETURN e").<TypeDescriptor>getColumn("e")).haveExactly(1, typeDescriptor(EnumerationType.class));
        assertThat(query("MATCH (e:Type:Enum)-[:EXTENDS]->(s) RETURN s").<TypeDescriptor>getColumn("s")).haveExactly(1, typeDescriptor(Enum.class));
        assertThat(query("MATCH (e:Type:Enum)-[:DECLARES]->(f:Field) RETURN f").<FieldDescriptor>getColumn("f")).haveExactly(1,
                fieldDescriptor(EnumerationType.class, "A"))
            .haveExactly(1, fieldDescriptor(EnumerationType.class, "B"))
            .haveExactly(1, fieldDescriptor(EnumerationType.class, "value"));
        assertThat(query("MATCH (e:Type:Enum)-[:DECLARES]->(c:Constructor) RETURN c").<ConstructorDescriptor>getColumn("c")).haveExactly(1,
            constructorDescriptor(EnumerationType.class, String.class, int.class, boolean.class));
        store.commitTransaction();
    }
}
