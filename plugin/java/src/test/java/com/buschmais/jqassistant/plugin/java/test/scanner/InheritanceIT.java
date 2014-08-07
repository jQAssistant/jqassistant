package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.inheritance.SubInterface;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.inheritance.SuperInterface;

public class InheritanceIT extends AbstractJavaPluginIT {

    /**
     * Verifies scanning of interface hierarchies.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void interfaces() throws IOException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(SuperInterface.class, SubInterface.class);
        store.beginTransaction();
        assertThat(query("MATCH (sub:Type:Interface)-[:IMPLEMENTS]->(super:Type:Interface) RETURN sub").getColumn("sub"),
                hasItem(typeDescriptor(SubInterface.class)));
        store.commitTransaction();
    }
}
