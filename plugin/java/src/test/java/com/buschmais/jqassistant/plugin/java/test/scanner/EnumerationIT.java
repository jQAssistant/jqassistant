package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.enumeration.EnumerationType;

/**
 * Contains test which verify correct scanning of constructors.
 */
public class EnumerationIT extends AbstractPluginIT {

    /**
     * Verifies scanning of {@link EnumerationType}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void implicitDefaultConstructor() throws IOException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(EnumerationType.class);
        store.beginTransaction();
        assertThat(query("MATCH (e:TYPE:ENUM) RETURN e").getColumn("e"), hasItem(typeDescriptor(EnumerationType.class)));
        assertThat(query("MATCH (e:TYPE:ENUM)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Enum.class)));
        assertThat(query("MATCH (e:TYPE:ENUM)-[:DECLARES]->(f:FIELD) RETURN f").getColumn("f"), CoreMatchers.allOf(
                hasItem(fieldDescriptor(EnumerationType.class, "A")), hasItem(fieldDescriptor(EnumerationType.class, "B")),
                hasItem(fieldDescriptor(EnumerationType.class, "value"))));
        assertThat(query("MATCH (e:TYPE:ENUM)-[:DECLARES]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
                hasItem(MethodDescriptorMatcher.constructorDescriptor(EnumerationType.class, String.class, int.class, boolean.class)));
        store.commitTransaction();
    }
}
