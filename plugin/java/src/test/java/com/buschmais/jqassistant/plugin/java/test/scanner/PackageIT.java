package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Contains tests regarding packages.
 */
public class PackageIT extends AbstractPluginIT {

    private static final String EMPTY_PACKAGE = "com.buschmais.jqassistant.plugin.java.test.set.scanner.empty";

    /**
     * Verifies that all packages are added to the artifact while scanning a directory.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void artifactContainsPackages() throws IOException {
        scanClassesDirectory(Pojo.class);
        // Assert that all packages of Pojo.class are contained in the artifact
        List<Matcher<? super Iterable<? super PackageDescriptor>>> packageMatchers = new ArrayList<>();
        String currentPackage = Pojo.class.getPackage().getName();
        do {
            packageMatchers.add(hasItem(packageDescriptor(currentPackage)));
            int separatorIndex = currentPackage.lastIndexOf('.');
            if (separatorIndex != -1) {
                currentPackage = currentPackage.substring(0, separatorIndex);
            } else {
                currentPackage = null;
            }
        } while (currentPackage != null);
        assertThat(query("MATCH (a:ARTIFACT)-[:CONTAINS]->(p:PACKAGE) WHERE a.FQN = 'artifact' RETURN p").getColumn("p"), allOf(packageMatchers));
        assertThat(query("MATCH (a:ARTIFACT)-[:CONTAINS]->(p:PACKAGE) WHERE a.FQN ='artifact' AND NOT p-[:CONTAINS]->(:TYPE) RETURN p").getColumn("p"), hasItem(packageDescriptor(EMPTY_PACKAGE)));
    }


    /**
     * Verifies that all packages containing elements have contains relations to their children.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void nonEmptyPackages() throws IOException {
        scanClassesDirectory(Pojo.class);
        TestResult query = query("MATCH (a:ARTIFACT)-[:CONTAINS]->(p:PACKAGE) WHERE a.FQN ='artifact' AND NOT p-[:CONTAINS]->() RETURN p");
        assertThat(query.getRows().size(), equalTo(1));
        assertThat(query.getColumn("p"), hasItem(packageDescriptor(EMPTY_PACKAGE)));
    }
}
