package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;

/**
 * Contains tests regarding packages.
 */
public class PackageIT extends AbstractJavaPluginIT {

    private static final String EMPTY_PACKAGE = "com.buschmais.jqassistant.plugin.java.test.set.scanner.empty";

    /**
     * Verifies that all packages are added to the artifact while scanning a
     * directory.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void artifactContainsPackages() throws IOException {
        scanClassPathDirectory(getClassesDirectory(Pojo.class));
        store.beginTransaction();
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
        assertThat(query("MATCH (a:Artifact:Directory)-[:CONTAINS]->(p:Package) WHERE a.fqn = 'artifact' RETURN p").getColumn("p"), allOf(packageMatchers));
        assertThat(
                query("MATCH (a:Artifact:Directory)-[:CONTAINS]->(p:Package) WHERE a.fqn ='artifact' AND NOT (p)-[:CONTAINS]->(:Type) RETURN p").getColumn("p"),
                hasItem(packageDescriptor(EMPTY_PACKAGE)));
        store.commitTransaction();
    }

    /**
     * Verifies that all packages containing elements have contains relations to
     * their children.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void nonEmptyPackages() throws IOException {
        scanClassPathDirectory(getClassesDirectory(Pojo.class));
        store.beginTransaction();
        TestResult query = query("MATCH (a:Artifact:Directory)-[:CONTAINS]->(p:Package) WHERE a.fqn ='artifact' AND NOT (p)-[:CONTAINS]->() RETURN p");
        assertThat(query.getRows().size(), equalTo(1));
        assertThat(query.getColumn("p"), hasItem(packageDescriptor(EMPTY_PACKAGE)));
        store.commitTransaction();
    }
}
