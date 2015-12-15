package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
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
        testResult = query("match (a:Artifact)-[:REQUIRES]->(t:Type) where a.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a1").get());
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
        testResult = query("match (a:Artifact)-[:REQUIRES]->(t:Type) where a.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a1").get());
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
        JavaArtifactFileDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactFileDescriptor a2 = getArtifactDescriptor("a2");
        store.create(a2, DependsOnDescriptor.class, a1);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a2", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a1").get());
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
        // java.lang.Object is only required by artifact A1
        testResult = query("match (a:Artifact)-[:REQUIRES]->(o:Type) where o.fqn={object} return a",
                MapBuilder.<String, Object> create("object", Object.class.getName()).get());
        List<JavaArtifactFileDescriptor> objects = testResult.getColumn("a");
        assertThat(objects.size(), equalTo(1));
        assertThat(objects.get(0), equalTo(a1));
        store.commitTransaction();
    }

    /**
     * Verifies scanning dependent types located in artifacts which are
     * transitively dependent.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void transitiveDependentArtifacts() throws IOException {
        store.beginTransaction();
        JavaArtifactFileDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactFileDescriptor a2 = getArtifactDescriptor("a2");
        JavaArtifactFileDescriptor a3 = getArtifactDescriptor("a3");
        store.create(a2, DependsOnDescriptor.class, a1);
        store.create(a3, DependsOnDescriptor.class, a2);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a3", B.class);
        store.beginTransaction();
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a1").get());
        assertThat(testResult.getRows().size(), equalTo(1));
        assertThat(testResult.getColumn("t"), hasItem(typeDescriptor(A.class)));
        testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a3").get());
        assertThat(testResult.getRows().size(), equalTo(1));
        assertThat(testResult.getColumn("t"), hasItem(typeDescriptor(B.class)));
        testResult = query(
                "match (artifact3:Artifact)-[:CONTAINS]->(b:Type)-[:DEPENDS_ON]->(a:Type)<-[:CONTAINS]-(artifact1:Artifact) where artifact1.fqn={a1} and artifact3.fqn={a3} and b.fqn={b} return a",
                MapBuilder.<String, Object> create("b", B.class.getName()).put("a1", "a1").put("a3", "a3").get());
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
        TestResult testResult = query("match (artifact:Artifact)-[:CONTAINS]->(t:Type) where artifact.fqn={artifact} return t",
                MapBuilder.<String, Object> create("artifact", "a1").get());
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
        ArtifactFileDescriptor a = (ArtifactFileDescriptor) testResult.getColumn("artifact").get(0);
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
        JavaArtifactFileDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactFileDescriptor a2 = getArtifactDescriptor("a2");
        JavaArtifactFileDescriptor a3 = getArtifactDescriptor("a3");
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
        JavaArtifactFileDescriptor otherArtifact = (JavaArtifactFileDescriptor) testResult.getColumn("otherArtifact").get(0);
        assertThat(otherArtifact, anyOf(equalTo(a1), equalTo(a2)));
        store.commitTransaction();
    }

    /**
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void duplicateTypeInSameArtifact() throws IOException {
        File directory = getClassesDirectory(A.class);
        final String resource = "/" + A.class.getName().replace(".", "/") + ".class";
        final File file = new File(directory, resource);
        scanClasses(B.class);
        List<? extends FileDescriptor> descriptors = execute("a1", new ScanClassPathOperation() {
            @Override
            public List<FileDescriptor> scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                List<FileDescriptor> result = new ArrayList<>();
                FileDescriptor fileDescriptor1 = scanner.scan(file, "/1.0" + resource, JavaScope.CLASSPATH);
                FileDescriptor fileDescriptor2 = scanner.scan(file, resource, JavaScope.CLASSPATH);
                result.add(fileDescriptor1);
                result.add(fileDescriptor2);
                return result;
            }
        });
        store.beginTransaction();
        assertThat(descriptors.size(), equalTo(2));
        FileDescriptor fileDescriptor1 = descriptors.get(0);
        assertThat(fileDescriptor1.getFileName(), equalTo("/1.0" + resource));
        assertThat(fileDescriptor1, instanceOf(TypeDescriptor.class));
        assertThat(((TypeDescriptor) fileDescriptor1).getFullQualifiedName(), equalTo(A.class.getName()));
        FileDescriptor fileDescriptor2 = descriptors.get(1);
        assertThat(fileDescriptor2.getFileName(), equalTo(resource));
        assertThat(fileDescriptor2, instanceOf(TypeDescriptor.class));
        assertThat(((TypeDescriptor) fileDescriptor2).getFullQualifiedName(), equalTo(A.class.getName()));
        store.commitTransaction();
    }
}
