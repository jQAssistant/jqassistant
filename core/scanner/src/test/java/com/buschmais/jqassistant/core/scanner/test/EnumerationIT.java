package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.test.matcher.descriptor.MethodDescriptorMatcher;
import com.buschmais.jqassistant.core.scanner.test.set.enumeration.EnumerationType;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Contains test which verify correct scanning of constructors.
 */
public class EnumerationIT extends AbstractScannerIT {

    /**
     * Verifies scanning of {@link EnumerationType}.
     *
     * @throws java.io.IOException   If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void implicitDefaultConstructor() throws IOException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(EnumerationType.class);
        assertThat(query("MATCH (e:TYPE:ENUM) RETURN e").getColumn("e"), hasItem(typeDescriptor(EnumerationType.class)));
        assertThat(query("MATCH (e:TYPE:ENUM)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Enum.class)));
        assertThat(query("MATCH (e:TYPE:ENUM)-[:CONTAINS]->(f:FIELD) RETURN f").getColumn("f"), CoreMatchers.allOf(hasItem(fieldDescriptor(EnumerationType.class, "A")), hasItem(fieldDescriptor(EnumerationType.class, "B")), hasItem(fieldDescriptor(EnumerationType
                .class, "value"))));
        assertThat(query("MATCH (e:TYPE:ENUM)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumn("c"), hasItem(MethodDescriptorMatcher.constructorDescriptor(EnumerationType.class, String.class, int.class, boolean.class)));
    }
}
