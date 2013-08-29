package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Verifies the DAO implementation.
 */
public class DAOIT extends AbstractScannerIT {

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
