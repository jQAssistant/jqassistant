package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
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
        assertThat(query("MATCH (a:Artifact:Directory:Container)-[:CONTAINS]->(p:Package) RETURN p").getColumn("p"), allOf(packageMatchers));
        assertThat(query("MATCH (a:Artifact:Directory)-[:CONTAINS]->(p:Package) WHERE NOT (p)-[:CONTAINS]->(:Type) RETURN p").getColumn("p"),
                hasItem(packageDescriptor(EMPTY_PACKAGE)));
        store.commitTransaction();
    }
}
