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
import java.util.*;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.annotations.AnnotatedType;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.annotations.Annotation;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.fieldaccesses.FieldAccess;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.fieldaccesses.FieldDependency;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.methodinvocations.MethodDependency;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.methodinvocations.MethodInvocation;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.packages.a.A;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.packages.b.B;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.parameters.Parameters;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.typebodies.FieldAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.typebodies.MethodAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.typebodies.TypeBody;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.types.DependentType;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.types.SuperType;
import com.buschmais.jqassistant.plugin.java.test.set.dependency.types.TypeAnnotation;

/**
 * Tests for the dependency concepts and result.
 */
public class DependencyIT extends AbstractPluginIT {

    /**
     * Verifies the concept "dependency:Annotation".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void annotations() throws IOException, AnalyzerException {
        scanClasses(AnnotatedType.class);
        applyConcept("dependency:Annotation");
        store.beginTransaction();
        assertThat(
                query("MATCH (e:TYPE:CLASS)-[:DEPENDS_ON]->(t:TYPE) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)),
                        hasItem(typeDescriptor(String.class))));
        assertThat(
                query("MATCH (e:FIELD)-[:DEPENDS_ON]->(t:TYPE) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)),
                        hasItem(typeDescriptor(String.class))));
        assertThat(
                query("MATCH (e:METHOD)-[:DEPENDS_ON]->(t:TYPE) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)),
                        hasItem(typeDescriptor(String.class))));
        assertThat(
                query("MATCH (e:PARAMETER)-[:DEPENDS_ON]->(t:TYPE) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(Annotation.class)), hasItem(typeDescriptor(Number.class)),
                        hasItem(typeDescriptor(String.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:MethodParameter".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void parameters() throws IOException, AnalyzerException {
        scanClasses(Parameters.class);
        applyConcept("dependency:MethodParameter");
        store.beginTransaction();
        assertThat(query("MATCH (m:METHOD)-[:DEPENDS_ON]->(t:TYPE) RETURN t").getColumn("t"),
                allOf(hasItem(typeDescriptor(String.class)), hasItem(typeDescriptor(Integer.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:MethodInvocation".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void methodInvocations() throws IOException, AnalyzerException {
        scanClasses(MethodInvocation.class, MethodDependency.class);
        applyConcept("dependency:MethodInvocation");
        store.beginTransaction();
        TestResult testResult = query("MATCH (m:METHOD)-[:DEPENDS_ON]->(t:TYPE) RETURN t");
        assertThat(
                testResult.getColumn("t"),
                allOf(hasItem(typeDescriptor(MethodDependency.class)), hasItem(typeDescriptor(Map.class)),
                        hasItem(typeDescriptor(SortedSet.class)), hasItem(typeDescriptor(Number.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:FieldAccess".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void fieldAccess() throws IOException, AnalyzerException {
        scanClasses(FieldAccess.class, FieldDependency.class);
        applyConcept("dependency:FieldAccess");
        store.beginTransaction();
        String query = "MATCH (m:METHOD)-[:DEPENDS_ON]->(t:TYPE) WHERE m.SIGNATURE =~ {method} RETURN t";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("method", "void readField.*");
        assertThat(query(query, parameters).getColumn("t"),
                allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Set.class))));
        parameters.put("method", "void writeField.*");
        assertThat(query(query, parameters).getColumn("t"),
                allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Set.class))));
        parameters.put("method", "void readStaticField.*");
        assertThat(query(query, parameters).getColumn("t"),
                allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Map.class))));
        parameters.put("method", "void writeStaticField.*");
        assertThat(query(query, parameters).getColumn("t"),
                allOf(hasItem(typeDescriptor(FieldDependency.class)), hasItem(typeDescriptor(Map.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:TypeBody".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void typeBodies() throws IOException, AnalyzerException {
        scanClasses(TypeBody.class);
        applyConcept("dependency:TypeBody");
        store.beginTransaction();
        TestResult testResult = query("MATCH (t1:TYPE)-[:DEPENDS_ON]->(t2:TYPE) RETURN t2");
        // field
        assertThat(
                testResult.getColumn("t2"),
                allOf(hasItem(typeDescriptor(List.class)), hasItem(typeDescriptor(String.class)),
                        hasItem(typeDescriptor(FieldAnnotation.class))));
        // method
        assertThat(
                testResult.getColumn("t2"),
                allOf(hasItem(typeDescriptor(Iterator.class)), hasItem(typeDescriptor(Number.class)),
                        hasItem(typeDescriptor(Integer.class)), hasItem(typeDescriptor(Exception.class)),
                        hasItem(typeDescriptor(Double.class)), hasItem(typeDescriptor(Boolean.class)),
                        hasItem(typeDescriptor(MethodAnnotation.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Type".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void types() throws IOException, AnalyzerException {
        scanClasses(DependentType.class);
        applyConcept("dependency:Type");
        store.beginTransaction();
        TestResult testResult = query("MATCH (t1:TYPE)-[:DEPENDS_ON]->(t2:TYPE) RETURN t2");
        // field
        assertThat(
                testResult.getColumn("t2"),
                allOf(hasItem(typeDescriptor(SuperType.class)), hasItem(typeDescriptor(Comparable.class)),
                        hasItem(typeDescriptor(Integer.class)), hasItem(typeDescriptor(TypeAnnotation.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Package".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void packages() throws IOException, AnalyzerException {
        scanClasses(A.class, B.class);
        applyConcept("dependency:Package");
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("package", A.class.getPackage().getName());
        assertThat(query("MATCH (p1:PACKAGE)-[:DEPENDS_ON]->(p2:PACKAGE) WHERE p1.FQN={package} RETURN p2", parameters).getColumn("p2"),
                hasItem(packageDescriptor(B.class.getPackage())));
        parameters.put("package", B.class.getPackage().getName());
        assertThat(query("MATCH (p1:PACKAGE)-[:DEPENDS_ON]->(p2:PACKAGE) WHERE p1.FQN={package} RETURN p2", parameters).getColumn("p2"),
                hasItem(packageDescriptor(A.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Artifact".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void artifacts() throws IOException, AnalyzerException {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        applyConcept("dependency:Artifact");
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("artifact", "a");
        assertThat(query("MATCH (a1:ARTIFACT)-[:DEPENDS_ON]->(a2:ARTIFACT) WHERE a1.FQN={artifact} RETURN a2", parameters).getColumn("a2"),
                hasItem(artifactDescriptor("b")));
        parameters.put("artifact", "b");
        assertThat(query("MATCH (a1:ARTIFACT)-[:DEPENDS_ON]->(a2:ARTIFACT) WHERE a1.FQN={artifact} RETURN a2", parameters).getColumn("a2"),
                hasItem(artifactDescriptor("a")));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:PackageCycles".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void packageCycles() throws IOException, AnalyzerException {
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
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void typeCycles() throws IOException, AnalyzerException {
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
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void artifactCycles() throws IOException, AnalyzerException {
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
