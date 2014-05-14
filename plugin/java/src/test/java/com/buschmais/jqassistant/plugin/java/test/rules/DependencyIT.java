package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.common.test.matcher.ArtifactDescriptorMatcher.artifactDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.annotations.AnnotatedType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.annotations.Annotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.fieldaccesses.FieldAccess;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.fieldaccesses.FieldDependency;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.methodinvocations.MethodDependency;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.methodinvocations.MethodInvocation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.a.A;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.b.B;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.parameters.Parameters;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.typebodies.FieldAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.typebodies.MethodAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.typebodies.TypeBody;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.DependentType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.SuperType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.TypeAnnotation;

/**
 * Tests for the dependency concepts and result.
 */
public class DependencyIT extends AbstractPluginIT {

    /**
     * Verifies the concept "dependency:Annotation".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void annotations() throws IOException, AnalysisException {
        scanClasses(AnnotatedType.class);
        applyConcept("dependency:Annotation");
        store.beginTransaction();
        assertThat(query("MATCH (e:Type:Class)-[:DEPENDS_ON]->(t:Type) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)), hasItem(typeDescriptor(String.class))));
        assertThat(query("MATCH (e:Field)-[:DEPENDS_ON]->(t:Type) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)), hasItem(typeDescriptor(String.class))));
        assertThat(query("MATCH (e:Method)-[:DEPENDS_ON]->(t:Type) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)), hasItem(typeDescriptor(String.class))));
        assertThat(query("MATCH (e:Parameter)-[:DEPENDS_ON]->(t:Type) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)), hasItem(typeDescriptor(String.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:MethodParameter".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void parameters() throws IOException, AnalysisException {
        scanClasses(Parameters.class);
        applyConcept("dependency:MethodParameter");
        store.beginTransaction();
        assertThat(query("MATCH (m:Method)-[:DEPENDS_ON]->(t:Type) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(String.class)), hasItem(typeDescriptor(Integer.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:MethodInvocation".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void methodInvocations() throws IOException, AnalysisException {
        scanClasses(MethodInvocation.class, MethodDependency.class);
        applyConcept("dependency:MethodInvocation");
        store.beginTransaction();
        TestResult testResult = query("MATCH (m:Method)-[:DEPENDS_ON]->(t:Type) RETURN t");
        assertThat(
                testResult.getColumn("t"),
                allOf(hasItem(typeDescriptor(MethodDependency.class)), hasItem(typeDescriptor(Map.class)), hasItem(typeDescriptor(SortedSet.class)),
                        hasItem(typeDescriptor(Number.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:FieldAccess".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void fieldAccess() throws IOException, AnalysisException {
        scanClasses(FieldAccess.class, FieldDependency.class);
        applyConcept("dependency:FieldAccess");
        store.beginTransaction();
        String query = "MATCH (m:Method)-[:DEPENDS_ON]->(t:Type) WHERE m.signature =~ {method} RETURN t";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("method", "void readField.*");
        assertThat(query(query, parameters).getColumn("t"), allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Set.class))));
        parameters.put("method", "void writeField.*");
        assertThat(query(query, parameters).getColumn("t"), allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Set.class))));
        parameters.put("method", "void readStaticField.*");
        assertThat(query(query, parameters).getColumn("t"), allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Map.class))));
        parameters.put("method", "void writeStaticField.*");
        assertThat(query(query, parameters).getColumn("t"), allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Map.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:TypeBody".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void typeBodies() throws IOException, AnalysisException {
        scanClasses(TypeBody.class);
        applyConcept("dependency:TypeBody");
        store.beginTransaction();
        TestResult testResult = query("MATCH (t1:Type)-[:DEPENDS_ON]->(t2:Type) RETURN t2");
        // field
        assertThat(testResult.getColumn("t2"),
                allOf(hasItem(typeDescriptor(List.class)), hasItem(typeDescriptor(String.class)), hasItem(typeDescriptor(FieldAnnotation.class))));
        // method
        assertThat(
                testResult.getColumn("t2"),
                allOf(hasItem(typeDescriptor(Iterator.class)), hasItem(typeDescriptor(Number.class)), hasItem(typeDescriptor(Integer.class)),
                        hasItem(typeDescriptor(Exception.class)), hasItem(typeDescriptor(Double.class)), hasItem(typeDescriptor(Boolean.class)),
                        hasItem(typeDescriptor(MethodAnnotation.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Type".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void types() throws IOException, AnalysisException {
        scanClasses(DependentType.class);
        applyConcept("dependency:Type");
        store.beginTransaction();
        TestResult testResult = query("MATCH (t1:Type)-[:DEPENDS_ON]->(t2:Type) RETURN t2");
        // field
        assertThat(
                testResult.getColumn("t2"),
                allOf(hasItem(typeDescriptor(SuperType.class)), hasItem(typeDescriptor(Comparable.class)), hasItem(typeDescriptor(Integer.class)),
                        hasItem(typeDescriptor(TypeAnnotation.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Package".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void packages() throws IOException, AnalysisException {
        scanClasses(A.class, B.class);
        applyConcept("dependency:Package");
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("package", A.class.getPackage().getName());
        assertThat(query("MATCH (p1:Package)-[:DEPENDS_ON]->(p2:Package) WHERE p1.fqn={package} RETURN p2", parameters).getColumn("p2"),
                hasItem(packageDescriptor(B.class.getPackage())));
        parameters.put("package", B.class.getPackage().getName());
        assertThat(query("MATCH (p1:Package)-[:DEPENDS_ON]->(p2:Package) WHERE p1.fqn={package} RETURN p2", parameters).getColumn("p2"),
                hasItem(packageDescriptor(A.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Artifact".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void artifacts() throws IOException, AnalysisException {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        applyConcept("dependency:Artifact");
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("artifact", "a");
        assertThat(query("MATCH (a1:Artifact)-[:DEPENDS_ON]->(a2:Artifact) WHERE a1.fqn={artifact} RETURN a2", parameters).getColumn("a2"),
                hasItem(artifactDescriptor("b")));
        parameters.put("artifact", "b");
        assertThat(query("MATCH (a1:Artifact)-[:DEPENDS_ON]->(a2:Artifact) WHERE a1.fqn={artifact} RETURN a2", parameters).getColumn("a2"),
                hasItem(artifactDescriptor("a")));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:PackageCycles".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void packageCycles() throws IOException, AnalysisException {
        scanClasses(A.class);
        scanClasses(B.class);
        validateConstraint("dependency:PackageCycles");
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:PackageCycles")));
        assertThat(constraintViolations, matcher);
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:TypeCycles".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void typeCycles() throws IOException, AnalysisException {
        scanClasses(A.class);
        scanClasses(B.class);
        validateConstraint("dependency:TypeCycles");
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:TypeCycles")));
        assertThat(constraintViolations, matcher);
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:ArtifactCycles".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void artifactCycles() throws IOException, AnalysisException {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        validateConstraint("dependency:ArtifactCycles");
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:ArtifactCycles")));
        assertThat(constraintViolations, matcher);
        store.commitTransaction();
    }
}
