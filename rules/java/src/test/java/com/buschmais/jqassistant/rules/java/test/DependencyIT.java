package com.buschmais.jqassistant.rules.java.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.core.model.api.Result;
import com.buschmais.jqassistant.core.model.api.rule.Constraint;
import com.buschmais.jqassistant.rules.java.test.set.dependency.fieldsormethods.FieldAnnotation;
import com.buschmais.jqassistant.rules.java.test.set.dependency.fieldsormethods.FieldOrMethodDependency;
import com.buschmais.jqassistant.rules.java.test.set.dependency.fieldsormethods.MethodAnnotation;
import com.buschmais.jqassistant.rules.java.test.set.dependency.packages.a.A;
import com.buschmais.jqassistant.rules.java.test.set.dependency.packages.b.B;
import com.buschmais.jqassistant.rules.java.test.set.dependency.types.DependentType;
import com.buschmais.jqassistant.rules.java.test.set.dependency.types.SuperType;
import com.buschmais.jqassistant.rules.java.test.set.dependency.types.TypeAnnotation;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.ArtifactDescriptorMatcher.artifactDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.rule.ConstraintMatcher.constraint;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the dependency concepts and result.
 */
public class DependencyIT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "dependency:FieldOrMethodDependency".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void fieldOrMethodDependency() throws IOException, AnalyzerException {
        scanClasses(FieldOrMethodDependency.class);
        applyConcept("dependency:FieldOrMethodDependency");
        TestResult testResult = executeQuery("MATCH (t1:TYPE)-[:DEPENDS_ON]->(t2:TYPE) RETURN t2");
        // field
        assertThat(testResult.getColumns().get("t2"), allOf(hasItem(typeDescriptor(List.class)), hasItem(typeDescriptor(String.class)), hasItem(typeDescriptor(FieldAnnotation.class))));
        // method
        assertThat(testResult.getColumns().get("t2"), allOf(hasItem(typeDescriptor(Iterator.class)), hasItem(typeDescriptor(Number.class)), hasItem(typeDescriptor(Integer.class)), hasItem(typeDescriptor(Exception.class)), hasItem(typeDescriptor(Double.class)), hasItem(typeDescriptor(Boolean.class)), hasItem(typeDescriptor(MethodAnnotation.class))));
    }

    /**
     * Verifies the concept "dependency:TypeDependency".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void typeDependency() throws IOException, AnalyzerException {
        scanClasses(DependentType.class);
        applyConcept("dependency:TypeDependency");
        TestResult testResult = executeQuery("MATCH (t1:TYPE)-[:DEPENDS_ON]->(t2:TYPE) RETURN t2");
        // field
        assertThat(testResult.getColumns().get("t2"), allOf(hasItem(typeDescriptor(SuperType.class)), hasItem(typeDescriptor(Comparable.class)), hasItem(typeDescriptor(Integer.class)), hasItem(typeDescriptor(TypeAnnotation.class))));
    }

    /**
     * Verifies the concept "dependency:PackageDependency".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void packageDependency() throws IOException, AnalyzerException {
        scanClasses(A.class, B.class);
        applyConcept("dependency:PackageDependency");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("package", A.class.getPackage().getName());
        assertThat(executeQuery("MATCH (p1:PACKAGE)-[:DEPENDS_ON]->(p2:PACKAGE) WHERE p1.FQN={package} RETURN p2", parameters).getColumns().get("p2"), hasItem(packageDescriptor(B.class.getPackage())));
        parameters.put("package", B.class.getPackage().getName());
        assertThat(executeQuery("MATCH (p1:PACKAGE)-[:DEPENDS_ON]->(p2:PACKAGE) WHERE p1.FQN={package} RETURN p2", parameters).getColumns().get("p2"), hasItem(packageDescriptor(A.class.getPackage())));
    }

    /**
     * Verifies the concept "dependency:ArtifactDependency".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void artifactDependency() throws IOException, AnalyzerException {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        applyConcept("dependency:ArtifactDependency");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("artifact", "a");
        assertThat(executeQuery("MATCH (a1:ARTIFACT)-[:DEPENDS_ON]->(a2:ARTIFACT) WHERE a1.FQN={artifact} RETURN a2", parameters).getColumns().get("a2"), hasItem(artifactDescriptor("b")));
        parameters.put("artifact", "b");
        assertThat(executeQuery("MATCH (a1:ARTIFACT)-[:DEPENDS_ON]->(a2:ARTIFACT) WHERE a1.FQN={artifact} RETURN a2", parameters).getColumns().get("a2"), hasItem(artifactDescriptor("a")));
    }

    /**
     * Verifies the constraint "dependency:CyclicPackageDependency".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void cyclicPackageDependency() throws IOException, AnalyzerException {
        scanClasses(A.class);
        scanClasses(B.class);
        validateConstraint("dependency:CyclicPackageDependency");
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:CyclicPackageDependency")));
        assertThat(constraintViolations, matcher);
    }

    /**
     * Verifies the constraint "dependency:CyclicTypeDependency".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void cyclicTypeDependency() throws IOException, AnalyzerException {
        scanClasses( A.class);
        scanClasses(B.class);
        validateConstraint("dependency:CyclicTypeDependency");
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:CyclicTypeDependency")));
        assertThat(constraintViolations, matcher);
    }

    /**
     * Verifies the constraint "dependency:CyclicArtifactDependency".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void cyclicArtifactDependency() throws IOException, AnalyzerException {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        validateConstraint("dependency:CyclicArtifactDependency");
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:CyclicArtifactDependency")));
        assertThat(constraintViolations, matcher);
    }}
