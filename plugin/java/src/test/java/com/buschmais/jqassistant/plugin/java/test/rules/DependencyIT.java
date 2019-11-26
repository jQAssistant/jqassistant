package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.a.A;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.b.B;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.DependentType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.FieldAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.FieldAnnotationValueType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.FieldType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.FieldTypeParameter;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.ImplementedInterface;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.ImplementedInterfaceTypeParameter;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.InvokeMethodType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.LocalVariable;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.MethodAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.MethodAnnotationValueType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.MethodException;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.MethodParameter;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.MethodParameterTypeParameter;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.MethodReturnType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.MethodReturnTypeParameter;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.SuperClass;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.SuperClassTypeParameter;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.TypeAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.TypeAnnotationValueType;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.common.test.matcher.ArtifactDescriptorMatcher.artifactDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the dependency concepts and result.
 */
public class DependencyIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "dependency:Type".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void types() throws IOException {
        scanClasses(DependentType.class);
        store.beginTransaction();
        TestResult testResult = query("MATCH (t1:Type)-[:DEPENDS_ON]->(t2:Type) RETURN t2");
        List<Object> dependencies = testResult.getColumn("t2");
        assertThat(dependencies, hasItem(typeDescriptor(TypeAnnotation.class)));
        assertThat(dependencies, hasItem(typeDescriptor(TypeAnnotationValueType.class)));
        assertThat(dependencies, hasItem(typeDescriptor(SuperClass.class)));
        assertThat(dependencies, hasItem(typeDescriptor(SuperClassTypeParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(ImplementedInterface.class)));
        assertThat(dependencies, hasItem(typeDescriptor(ImplementedInterfaceTypeParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(FieldAnnotation.class)));
        assertThat(dependencies, hasItem(typeDescriptor(FieldAnnotationValueType.class)));
        assertThat(dependencies, hasItem(typeDescriptor(FieldType.class)));
        assertThat(dependencies, hasItem(typeDescriptor(FieldTypeParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodAnnotation.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodAnnotationValueType.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodAnnotation.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodReturnType.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodReturnTypeParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodAnnotation.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodParameterTypeParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(MethodException.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(LocalVariableAnnotation.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(LocalVariableAnnotationValueType.class)));
        assertThat(dependencies, hasItem(typeDescriptor(LocalVariable.class)));
        assertThat(dependencies, hasItem(typeDescriptor(LocalVariable.ReadStaticVariable.class)));
        assertThat(dependencies, hasItem(typeDescriptor(LocalVariable.ReadVariable.class)));
        assertThat(dependencies, hasItem(typeDescriptor(LocalVariable.WriteStaticVariable.class)));
        assertThat(dependencies, hasItem(typeDescriptor(LocalVariable.WriteVariable.class)));
        assertThat(dependencies, hasItem(typeDescriptor(InvokeMethodType.class)));
        assertThat(dependencies, hasItem(typeDescriptor(InvokeMethodType.InvokeMethodReturnType.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(InvokeMethodType.InvokeMethodReturnTypeParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(InvokeMethodType.InvokeMethodParameterType.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(InvokeMethodType.InvokeMethodParameterTypeTypeParameter.class)));
        assertThat(dependencies, hasItem(typeDescriptor(InvokeMethodType.InvokeMethodException.class)));
        store.commitTransaction();
    }

    @Test
    public void weight() throws IOException {
        scanClasses(DependentType.class);
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("t1", DependentType.class.getName()).put("t2", LocalVariable.class.getName()).get();
        List<Map<String, Object>> rows = query("MATCH (t1:Type)-[d:DEPENDS_ON]->(t2:Type) WHERE t1.fqn={t1} and t2.fqn={t2} RETURN d", params).getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        TypeDependsOnDescriptor dependsOn = (TypeDependsOnDescriptor) row.get("d");
        assertThat(dependsOn.getWeight(), equalTo(7));
        store.commitTransaction();
    }

    @Test
    public void weightDisabled() throws IOException {
        Map<String, Object> pluginConfig = MapBuilder.<String, Object> create(ClassFileScannerPlugin.PROPERTY_TYPE_DEPENDS_ON_WEIGHT, "false").get();
        File classesDirectory = getClassesDirectory(DependencyIT.class);
        store.beginTransaction();
        getScanner(pluginConfig).scan(classesDirectory, "/", JavaScope.CLASSPATH);
        List<Map<String, Object>> rows = query("MATCH (:Type)-[d:DEPENDS_ON]->(:Type) WHERE exists(d.weight) RETURN d").getRows();
        assertThat(rows.size(), equalTo(0));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Package".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void packages() throws Exception {
        scanClassPathDirectory(getClassesDirectory(DependencyIT.class));
        assertThat(applyConcept("dependency:Package").getStatus(), Matchers.equalTo(SUCCESS));
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
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void artifacts() throws Exception {
        store.beginTransaction();
        JavaArtifactFileDescriptor a = getArtifactDescriptor("a");
        JavaArtifactFileDescriptor b = getArtifactDescriptor("b");
        store.create(b, DependsOnDescriptor.class, a);
        store.commitTransaction();
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        assertThat(applyConcept("dependency:Artifact").getStatus(), Matchers.equalTo(SUCCESS));
        store.beginTransaction();
        verifyArtifactDependency("a", "b");
        verifyArtifactDependency("b", "a");
        store.commitTransaction();
    }

    private Map<String, Object> verifyArtifactDependency(String from, String to) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("artifact", from);
        List<Object> usedDependency = query("MATCH (a1:Artifact)-[:DEPENDS_ON{used:true}]->(a2:Artifact) WHERE a1.fqn={artifact} RETURN a2", parameters)
                .getColumn("a2");
        assertThat(usedDependency, hasItem(artifactDescriptor(to)));
        // The DEPENDS_RELATION must be unique
        List<Object> dependency = query("MATCH (a1:Artifact)-[:DEPENDS_ON]->(a2:Artifact) WHERE a1.fqn={artifact} RETURN a2", parameters).getColumn("a2");
        assertThat(dependency.size(), equalTo(1));
        return parameters;
    }

    /**
     * Verifies the constraint "dependency:PackageCycles".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void packageCycles() throws Exception {
        scanClassPathDirectory(getClassesDirectory(A.class));
        assertThat(validateConstraint("dependency:PackageCycles").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        Map<String, Result<Constraint>> constraintViolations = reportPlugin.getConstraintResults();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:PackageCycles")));
        assertThat(constraintViolations.values(), matcher);
        Result<Constraint> result = constraintViolations.get("dependency:PackageCycles");
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(2));
        for (Map<String, Object> row : rows) {
            PackageDescriptor p = (PackageDescriptor) row.get("Package");
            assertThat(p.getFullQualifiedName(), anyOf(equalTo(A.class.getPackage().getName()), equalTo(B.class.getPackage().getName())));
        }
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:ArtifactCycles".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void artifactCycles() throws Exception {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        assertThat(validateConstraint("dependency:ArtifactCycles").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        Collection<Result<Constraint>> constraintViolations = reportPlugin.getConstraintResults().values();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:ArtifactCycles")));
        assertThat(constraintViolations, matcher);
        store.commitTransaction();
    }
}
