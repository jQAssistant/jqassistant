package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.Assert.assertThat;

/**
 * Verifies the DAO implementation.
 */
public class DAOIT extends AbstractPluginIT {

    /**
     * Verifies that a query collection of nodes returned by a query is correctly decoded into descriptors.
     * @throws IOException If the test fails.
     */
    @Test
    public void resultContainsCollection() throws IOException {
        scanClasses(PojoIT.class);
        TestResult query = query("MATCH parent:PACKAGE-[:CONTAINS]->child:PACKAGE RETURN parent, COLLECT(child) AS children");
        Iterable<PackageDescriptor> parent = query.getColumn("parent");
        assertThat(parent, everyItem(any(PackageDescriptor.class)));
        Iterable<Iterable<PackageDescriptor>> children = query.getColumn("children");
        assertThat(children, everyItem(everyItem(any(PackageDescriptor.class))));
    }

}
