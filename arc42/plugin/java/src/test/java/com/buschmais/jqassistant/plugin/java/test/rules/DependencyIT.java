package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.a.A;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.b.B;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.common.test.matcher.ArtifactDescriptorMatcher.artifactDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Tests for the dependency concepts and result.
 */
class DependencyIT extends AbstractJavaPluginIT {

    @Override
    protected Map<String, Object> getScannerProperties() {
        return Map.of(ClassFileScannerPlugin.PROPERTY_INCLUDE_LOCAL_VARIABLES, true);
    }

    /**
     * Verifies type dependencies.
     */
    @Test
    void types() {
        scanClasses(DependentType.class);

        store.beginTransaction();
        TestResult testResult = query("MATCH (t1:Type)-[:DEPENDS_ON]->(t2:Type) RETURN t2");
        List<TypeDescriptor> dependencies = testResult.getColumn("t2");
        List<Class<?>> expectedDependencies = List.of(TypeAnnotation.class, TypeAnnotationValueType.class, SuperClass.class, SuperClassTypeParameter.class,
            ImplementedInterface.class, ImplementedInterfaceTypeParameter.class, FieldAnnotation.class, FieldAnnotationValueType.class, FieldType.class,
            FieldTypeParameter.class, MethodAnnotation.class, MethodAnnotationValueType.class, MethodAnnotation.class, MethodReturnType.class,
            MethodReturnTypeParameter.class, MethodParameter.class, MethodParameterTypeParameter.class, MethodException.class,
            // LocalVariableAnnotation.class,
            // LocalVariableAnnotationValueType.class,
            LocalVariable.class, LocalVariable.ReadStaticVariable.class, LocalVariable.ReadVariable.class, LocalVariable.WriteStaticVariable.class,
            LocalVariable.WriteVariable.class, InvokeMethodType.class, InvokeMethodType.InvokeMethodReturnType.class,
            // InvokeMethodType.InvokeMethodReturnTypeParameter.class,
            InvokeMethodType.InvokeMethodParameterType.class,
            // InvokeMethodType.InvokeMethodParameterTypeTypeParameter.class,
            InvokeMethodType.InvokeMethodException.class);
        assertThat(dependencies).hasSize(expectedDependencies.size());
        for (Class<?> expectedDependency : expectedDependencies) {
            assertThat(dependencies).haveExactly(1, typeDescriptor(expectedDependency));
        }

        store.commitTransaction();
    }

    @Test
    void weight() {
        scanClasses(DependentType.class);
        store.beginTransaction();
        Map<String, Object> params = Map.of("t1", DependentType.class.getName(), "t2", LocalVariable.class.getName());
        List<Map<String, Object>> rows = query("MATCH (t1:Type)-[d:DEPENDS_ON]->(t2:Type) WHERE t1.fqn=$t1 and t2.fqn=$t2 RETURN d", params).getRows();
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.get(0);
        TypeDependsOnDescriptor dependsOn = (TypeDependsOnDescriptor) row.get("d");
        assertThat(dependsOn.getWeight()).isEqualTo(7);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:PackageDependency".
     *
     * @throws IOException
     *     If the test fails.
     */
    @Test
    void packages() throws Exception {
        scanClassPathDirectory(getClassesDirectory(DependencyIT.class));
        assertThat(applyConcept("java:PackageDependency").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        List<Map<String, Object>> rows = query("MATCH (p1:Package)-[d:DEPENDS_ON]->(p2:Package) WHERE p1.fqn=$package RETURN p2, d.weight as weight",
            Map.of("package", A.class.getPackage()
                .getName())).getRows();
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.get(0);
        assertThat((PackageDescriptor) row.get("p2"), packageDescriptor(B.class.getPackage()));
        assertThat(row).containsEntry("weight", 1L);
        assertThat(query("MATCH (p1:Package)-[:DEPENDS_ON]->(p2:Package) WHERE p1.fqn=$package RETURN p2", Map.of("package", B.class.getPackage()
            .getName())).getColumn("p2"), hasItem(packageDescriptor(A.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Artifact".
     *
     * @throws IOException
     *     If the test fails.
     */
    @Test
    void artifacts() throws Exception {
        store.beginTransaction();
        JavaArtifactFileDescriptor a = getArtifactDescriptor("a");
        JavaArtifactFileDescriptor b = getArtifactDescriptor("b");
        store.create(b, DependsOnDescriptor.class, a);
        store.commitTransaction();
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        assertThat(applyConcept("java:ArtifactDependency").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        verifyArtifactDependency("a", "b");
        verifyArtifactDependency("b", "a");
        store.commitTransaction();
    }

    private void verifyArtifactDependency(String from, String to) {
        List<Map<String, Object>> rows = query(
            "MATCH (a1:Artifact)-[dependsOn:DEPENDS_ON]->(a2:Artifact) WHERE a1.fqn=$artifact RETURN a2, dependsOn.weight as weight",
            Map.of("artifact", from)).getRows();
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.get(0);
        assertThat((ArtifactFileDescriptor) row.get("a2"), artifactDescriptor(to));
        assertThat(row).containsEntry("weight", 1L);
    }

    /**
     * Verifies the constraint "java:AvoidCyclicPackageDependencies".
     *
     * @throws IOException
     *     If the test fails.
     */
    @Test
    void packageCycles() throws Exception {
        scanClassPathDirectory(getClassesDirectory(A.class));

        Result<Constraint> result = validateConstraint("java:AvoidCyclicPackageDependencies");

        assertThat(result.getStatus()).isEqualTo(FAILURE);
        store.beginTransaction();
        List<Row> rows = result.getRows();
        assertThat(rows).hasSize(2);
        for (Row row : rows) {
            PackageDescriptor p = (PackageDescriptor) row.getColumns()
                .get("Package")
                .getValue();
            assertThat(p.getFullQualifiedName()).satisfiesAnyOf(name -> assertThat(name).isEqualTo(A.class.getPackage()
                .getName()), name -> assertThat(name).isEqualTo(B.class.getPackage()
                .getName()));
        }
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "java:AvoidCyclicArtifactDependencies".
     *
     * @throws IOException
     *     If the test fails.
     */
    @Test
    void artifactCycles() throws Exception {
        scanClasses("a", A.class);
        scanClasses("b", B.class);

        Result<Constraint> result = validateConstraint("java:AvoidCyclicArtifactDependencies");

        assertThat(result.getStatus()).isEqualTo(FAILURE);
        store.beginTransaction();
        List<Row> rows = result.getRows();
        assertThat(rows).hasSize(2);
        for (Row row : rows) {
            ArtifactDescriptor p = (ArtifactDescriptor) row.getColumns()
                .get("Artifact")
                .getValue();
            assertThat(p.getFullQualifiedName()).satisfiesAnyOf(name -> assertThat(name).isEqualTo("a"), name -> assertThat(name).isEqualTo("b"));
        }
        store.commitTransaction();
    }
}
