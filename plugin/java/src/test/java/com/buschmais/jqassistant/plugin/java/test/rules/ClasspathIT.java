package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.*;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.b.DependentType;

/**
 * Tests for the dependency concepts and result.
 */
public class ClasspathIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "classpath:resolveType".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveType() throws IOException, AnalysisException {
        scanAndApply("classpath:ResolveType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("a1", "b").put("a2", "a").get();
        List<TypeDescriptor> resolvedTypes = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(t1:Type)-[:RESOLVES_TO]->(rt:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn={a1} and a2.fqn={a2} RETURN rt",
                params).getColumn("rt");
        assertThat(resolvedTypes.size(), equalTo(6));
        assertThat(
                resolvedTypes,
                hasItems(typeDescriptor(AnnotationType.class), typeDescriptor(ClassType.class), typeDescriptor(InterfaceType.class),
                        typeDescriptor(EnumType.class), typeDescriptor(ExceptionType.class), typeDescriptor(ValueType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveMember".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveMember() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanAndApply("classpath:ResolveMember");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("a1", "b").put("a2", "a").get();
        // Methods
        List<MethodDescriptor> resolvedMethods = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(:Type)-[:DECLARES]->()-[:RESOLVES_TO]->(rm:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn={a1} and a2.fqn={a2} RETURN rm",
                params).getColumn("rm");
        assertThat(resolvedMethods.size(), equalTo(2));
        assertThat(resolvedMethods, hasItems(constructorDescriptor(ClassType.class), methodDescriptor(ClassType.class, "bar")));
        // Fields
        List<FieldDescriptor> resolvedFields = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(:Type)-[:DECLARES]->()-[:RESOLVES_TO]->(rf:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn={a1} and a2.fqn={a2} RETURN rf",
                params).getColumn("rf");
        assertThat(resolvedFields.size(), equalTo(2));
        assertThat(resolvedFields, hasItems(fieldDescriptor(ClassType.class, "foo"), fieldDescriptor(EnumType.B)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveDependency".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveDependency() throws IOException, AnalysisException {
        scanAndApply("classpath:ResolveDependency");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> dependencies = query(
                "MATCH (dependentType:Type)-[:DEPENDS_ON{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(dependencies.size(), equalTo(6));
        assertThat(
                dependencies,
                hasItems(typeDescriptor(AnnotationType.class), typeDescriptor(ClassType.class), typeDescriptor(InterfaceType.class),
                        typeDescriptor(EnumType.class), typeDescriptor(ExceptionType.class), typeDescriptor(ValueType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveExtends".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveExtends() throws IOException, AnalysisException {
        scanAndApply("classpath:ResolveExtends");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> dependencies = query(
                "MATCH (dependentType:Type)-[:EXTENDS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(dependencies.size(), equalTo(1));
        assertThat(dependencies, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveImplements".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveImplements() throws IOException, AnalysisException {
        scanAndApply("classpath:ResolveImplements");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> dependencies = query(
                "MATCH (dependentType:Type)-[:IMPLEMENTS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(dependencies.size(), equalTo(1));
        assertThat(dependencies, hasItems(typeDescriptor(InterfaceType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveFieldType".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveFieldType() throws IOException, AnalysisException {
        scanAndApply("classpath:ResolveFieldType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("f", "annotatedField")
                .put("a", "a").get();
        List<TypeDescriptor> dependencies = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and f.name={f} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(dependencies.size(), equalTo(1));
        assertThat(dependencies, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    private void scanAndApply(String concept) throws IOException, AnalysisException {
        scanClasses("a", ClassType.class, InterfaceType.class, AnnotationType.class, EnumType.class, ExceptionType.class, ValueType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept(concept).getStatus(), equalTo(SUCCESS));
    }
}
