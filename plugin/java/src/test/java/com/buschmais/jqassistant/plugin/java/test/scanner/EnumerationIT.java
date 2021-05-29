package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.enumeration.EnumerationType;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Contains test which verify correct scanning of constructors.
 */
public class EnumerationIT extends AbstractJavaPluginIT {

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
        assertThat(query("MATCH (e:Type:Enum) RETURN e").getColumn("e"), hasItem(typeDescriptor(EnumerationType.class)));
        assertThat(query("MATCH (e:Type:Enum)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Enum.class)));
        assertThat(query("MATCH (e:Type:Enum)-[:DECLARES]->(f:Field) RETURN f").getColumn("f"), hasItems(fieldDescriptor(EnumerationType.class, "A"),
                fieldDescriptor(EnumerationType.class, "B"), fieldDescriptor(EnumerationType.class, "value")));
        assertThat(query("MATCH (e:Type:Enum)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(MethodDescriptorMatcher.constructorDescriptor(EnumerationType.class, String.class, int.class, boolean.class)));
        store.commitTransaction();
    }
}
