package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.resolver.A;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.resolver.B;

public class TypeResolverIT extends AbstractJavaPluginIT {

    /**
     * Verify scanning dependent types in one artifact where the dependent type
     * is scanned first.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void dependentTypeFirst() throws IOException {
        scanClasses("a1", B.class);
        scanClasses("a1", A.class);
        store.beginTransaction();
        TestResult testResult = query("match (a:Artifact)-[:CONTAINS]->(t:Type) where a.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a1").get());
        assertThat(testResult.getRows().size(), equalTo(2));
        assertThat(testResult.getColumn("t"), allOf(hasItem(typeDescriptor(A.class)), hasItem(typeDescriptor(B.class))));
        testResult = query("match (a:Artifact)-[:REQUIRES]->(t:Type) where a.fqn={artifact} return t", MapBuilder.<String, Object> create("artifact", "a1")
                .get());
        assertThat(testResult.getColumn("t"), allOf(not(hasItem(typeDescriptor(A.class))), not(hasItem(typeDescriptor(B.class)))));
        store.commitTransaction();
    }

    /**
     * Verify scanning dependent types in one artifact where the dependency is
     * scanned first.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void dependencyTypeFirst() throws IOException {
        scanClasses("a1", A.class);
        scanClasses("a1", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (a:Artifact)-[:CONTAINS]->(t:Type) where a.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a1").get());
        assertThat(testResult.getRows().size(), equalTo(2));
        assertThat(testResult.getColumn("t"), allOf(hasItem(typeDescriptor(A.class)), hasItem(typeDescriptor(B.class))));
        testResult = query("match (a:Artifact)-[:REQUIRES]->(t:Type) where a.fqn={artifact} return t", MapBuilder.<String, Object> create("artifact", "a1")
                .get());
        assertThat(testResult.getColumn("t"), allOf(not(hasItem(typeDescriptor(A.class))), not(hasItem(typeDescriptor(B.class)))));
        store.commitTransaction();
    }

    /**
     * Verifies scanning dependent types located in dependent artifacts.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void dependentArtifacts() throws IOException {
        store.beginTransaction();
        JavaArtifactDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactDescriptor a2 = getArtifactDescriptor("a2");
        store.create(a2, DependsOnDescriptor.class, a1);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a2", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t", MapBuilder
                .<String, Object> create("artifact", "a1").get());
        assertThat(testResult.getRows().size(), equalTo(1));
        assertThat(testResult.getColumn("t"), hasItem(typeDescriptor(A.class)));
        testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a2").get());
        assertThat(testResult.getRows().size(), equalTo(1));
        assertThat(testResult.getColumn("t"), hasItem(typeDescriptor(B.class)));
        testResult = query(
                "match (artifact2:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)<-[:CONTAINS]-(artifact1:Artifact) where artifact1.fqn={a1} and artifact2.fqn={a2} and b.fqn={b} return a",
                MapBuilder.<String, Object> create("b", B.class.getName()).put("a1", "a1").put("a2", "a2").get());
        assertThat(testResult.getColumn("a"), hasItem(typeDescriptor(A.class)));
        store.commitTransaction();
    }

    /**
     * Verifies scanning dependent types located in independent artifacts.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void independentArtifacts() throws IOException {
        scanClasses("a1", A.class);
        scanClasses("a2", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t", MapBuilder
                .<String, Object> create("artifact", "a1").get());
        assertThat(testResult.getRows().size(), equalTo(1));
        assertThat(testResult.getColumn("t"), hasItem(typeDescriptor(A.class)));
        testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a2").get());
        assertThat(testResult.getRows().size(), equalTo(1));
        assertThat(testResult.getColumn("t"), hasItem(typeDescriptor(B.class)));
        testResult = query(
                "match (artifact2:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)<-[:CONTAINS]-(artifact1:Artifact) where artifact1.fqn={a1} and artifact2.fqn={a2} and b.fqn={b} return a",
                MapBuilder.<String, Object> create("b", B.class.getName()).put("a1", "a1").put("a2", "a2").get());
        assertThat(testResult.getRows().size(), equalTo(0));
        testResult = query("match (artifact:Artifact)-[:REQUIRES]->(a:Type) where a.fqn={a} return artifact",
                MapBuilder.<String, Object> create("a", A.class.getName()).get());
        assertThat(testResult.getRows().size(), equalTo(1));
        ArtifactDescriptor a = (ArtifactDescriptor) testResult.getColumn("artifact").get(0);
        assertThat(a.getFullQualifiedName(), equalTo("a2"));
        store.commitTransaction();
    }

    /**
     * Verifies scanning the same type which exists in two independent
     * artifacts.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void duplicateType() throws IOException {
        scanClasses("a1", A.class);
        scanClasses("a2", A.class);
        store.beginTransaction();
        TestResult testResult = query("match (:Artifact)-[:CONTAINS]->(t:Type) where t.fqn={t} return t",
                MapBuilder.<String, Object> create("t", A.class.getName()).get());
        assertThat(testResult.getRows().size(), equalTo(2));
        store.commitTransaction();
    }

    /**
     * Verifies scanning a type depending on another type which exists in two
     * independent artifacts.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void ambiguousDependencies() throws IOException {
        store.beginTransaction();
        JavaArtifactDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactDescriptor a2 = getArtifactDescriptor("a2");
        JavaArtifactDescriptor a3 = getArtifactDescriptor("a3");
        store.create(a3, DependsOnDescriptor.class, a1);
        store.create(a3, DependsOnDescriptor.class, a2);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a2", A.class);
        scanClasses("a3", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (:Artifact)-[:CONTAINS]->(t:Type) where t.fqn={t} return t",
                MapBuilder.<String, Object> create("t", A.class.getName()).get());
        assertThat(testResult.getRows().size(), equalTo(2));
        testResult = query(
                "match (artifact3:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)-[:CONTAINS]-(otherArtifact:Artifact) where b.fqn={b} return otherArtifact",
                MapBuilder.<String, Object> create("a", A.class.getName()).put("b", B.class.getName()).get());
        assertThat(testResult.getRows().size(), equalTo(1));
        JavaArtifactDescriptor otherArtifact = (JavaArtifactDescriptor) testResult.getColumn("otherArtifact").get(0);
        assertThat(otherArtifact, anyOf(equalTo(a1), equalTo(a2)));
        store.commitTransaction();
    }
}
