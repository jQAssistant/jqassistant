package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.*;
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
    public void resolveType() throws Exception {
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
     * Verifies the uniqueness of concept "classpath:resolveType" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveTypeUnique() throws Exception {
        scanClasses("a", ClassType.class, InterfaceType.class, ExceptionType.class);
        scanClasses("b", DependentType.class);
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (:Artifact {fqn: 'b'})-[:REQUIRES]->(t1:Type {name: 'ClassType'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t2:Class {name: 'ClassType'}) MERGE (t1)-[r:RESOLVES_TO {prop: 'value'}]->(t2) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (:Artifact {fqn: 'b'})-[:REQUIRES]->(t1:Type {name: 'InterfaceType'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t2:Interface {name: 'InterfaceType'}) MERGE (t1)-[r:RESOLVES_TO]->(t2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("RESOLVES_TO", 2);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("RESOLVES_TO", 3);
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
    public void resolveMember() throws Exception {
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
     * Verifies the uniqueness of concept "classpath:resolveMember" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveMemberUnique() throws Exception {
        scanClasses("a", ClassType.class, EnumType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (f1:Field)<-[:DECLARES]-(:Type)-[:RESOLVES_TO]->(:Type)-[:DECLARES]->(f2:Field) WHERE f1.signature=f2.signature AND f2.name='foo' MERGE (f1)-[r:RESOLVES_TO {prop: 'value', resolved: false}]->(f2) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (f1:Field)<-[:DECLARES]-(:Type)-[:RESOLVES_TO]->(:Type)-[:DECLARES]->(f2:Field) WHERE f1.signature=f2.signature AND f2.name='B' MERGE (f1)-[r:RESOLVES_TO]->(f2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("RESOLVES_TO", 4, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("RESOLVES_TO", 6, 4, 0);
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
    public void resolveDependency() throws Exception {
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
     * Verifies the uniqueness of concept "classpath:resolveDependency" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveDependencyUnique() throws Exception {
        scanClasses("a", ClassType.class, InterfaceType.class, EnumType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
    	store.beginTransaction();
    	// create existing relations with and without properties
        assertThat(query("MATCH (t1:Type {name: 'DependentType'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t2:Type {name: 'ClassType'}) MERGE (t1)-[r:DEPENDS_ON {prop: 'value', resolved: false}]->(t2) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t1:Type {name: 'DependentType'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t2:Type {name: 'EnumType'}) MERGE (t1)-[r:DEPENDS_ON]->(t2) RETURN r").getColumn("r").size(), equalTo(1));
      	verifyUniqueRelation("DEPENDS_ON", 17, 0, 1);
      	store.commitTransaction();
		assertThat(applyConcept("classpath:ResolveDependency").getStatus(), equalTo(SUCCESS));
		store.beginTransaction();
		verifyUniqueRelation("DEPENDS_ON", 18, 3, 0);
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
    public void resolveExtends() throws Exception {
        scanAndApply("classpath:ResolveExtends");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> extendedTypes = query(
                "MATCH (dependentType:Type)-[:EXTENDS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(extendedTypes.size(), equalTo(1));
        assertThat(extendedTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "classpath:resolveExtends" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveExtendsUnique() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
    	store.beginTransaction();
    	// create existing relations with properties
        assertThat(query("MATCH (t1:Type {name: 'DependentType'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t2:Type {name: 'ClassType'}) MERGE (t1)-[r:EXTENDS {prop: 'value', resolved: false}]->(t2) RETURN r").getColumn("r").size(), equalTo(1));
		verifyUniqueRelation("EXTENDS", 3, 0, 1);
		store.commitTransaction();
		assertThat(applyConcept("classpath:ResolveExtends").getStatus(), equalTo(SUCCESS));
		store.beginTransaction();
		verifyUniqueRelation("EXTENDS", 3, 1, 0);
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
    public void resolveImplements() throws Exception {
        scanAndApply("classpath:ResolveImplements");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> implementedTypes = query(
                "MATCH (dependentType:Type)-[:IMPLEMENTS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(implementedTypes.size(), equalTo(1));
        assertThat(implementedTypes, hasItems(typeDescriptor(InterfaceType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "classpath:resolveImplements" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveImplementsUnique() throws Exception {
        scanClasses("a", InterfaceType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
    	store.beginTransaction();
    	// create existing relations with properties
        assertThat(query("MATCH (t1:Type {name: 'DependentType'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t2:Type {name: 'InterfaceType'}) MERGE (t1)-[r:IMPLEMENTS {prop: 'value', resolved: false}]->(t2) RETURN r").getColumn("r").size(), equalTo(1));
		verifyUniqueRelation("IMPLEMENTS", 2, 0, 1);
		store.commitTransaction();
		assertThat(applyConcept("classpath:ResolveImplements").getStatus(), equalTo(SUCCESS));
		store.beginTransaction();
		verifyUniqueRelation("IMPLEMENTS", 2, 1, 0);
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
    public void resolveFieldType() throws Exception {
        scanAndApply("classpath:ResolveFieldType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("f", "field").put("a", "a").get();
        List<TypeDescriptor> fieldTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and f.name={f} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(fieldTypes.size(), equalTo(1));
        assertThat(fieldTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "classpath:resolveFieldType" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveFieldTypeUnique() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties
        assertThat(query("MATCH (f:Field {name: 'field'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t:Type {name: 'ClassType'}) MERGE (f)-[r:OF_TYPE {prop: 'value', resolved: false}]->(t) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("OF_TYPE", 11, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveFieldType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("OF_TYPE", 11, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveThrows".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveThrows() throws Exception {
        scanAndApply("classpath:ResolveThrows");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> exceptionTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:THROWS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(exceptionTypes.size(), equalTo(1));
        assertThat(exceptionTypes, hasItems(typeDescriptor(ExceptionType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveThrows" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveThrowsUnique() throws Exception {
        scanClasses("a", ExceptionType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
      	// create existing relations with properties
        assertThat(query("MATCH (m:Method {name: 'signature'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t:Type {name: 'ExceptionType'}) MERGE (m)-[r:THROWS {prop: 'value', resolved: false}]->(t) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("THROWS", 2, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveThrows").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("THROWS", 2, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveReturns".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveReturns() throws Exception {
        scanAndApply("classpath:ResolveReturns");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> returnTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:RETURNS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(returnTypes.size(), equalTo(1));
        assertThat(returnTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveReturns" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveReturnsUnique() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (m:Method {name: 'signature'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t:Type {name: 'ClassType'}) MERGE (m)-[r:RETURNS {prop: 'value', resolved: false}]->(t) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("RETURNS", 7, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveReturns").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("RETURNS", 7, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveParameterType".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveParameterType() throws Exception {
        scanAndApply("classpath:ResolveParameterType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> parameterTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(parameterTypes.size(), equalTo(1));
        assertThat(parameterTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveParameterType" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveParameterTypeUnique() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (:Method {name: 'signature'})-[:HAS]->(p:Parameter), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t:Type {name: 'ClassType'}) MERGE (p)-[r:OF_TYPE {prop: 'value', resolved: false}]->(t) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (:Method {name: 'fieldAccess'})-[:HAS]->(p:Parameter), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t:Type {name: 'ClassType'}) MERGE (p)-[r:OF_TYPE]->(t) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("OF_TYPE", 12, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveParameterType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("OF_TYPE", 13, 3, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveAnnotationType".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveAnnotationType() throws Exception {
        scanAndApply("classpath:ResolveAnnotationType");
        store.beginTransaction();
        // type annotation
        Map<String, Object> typeParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> typeAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN t",
                typeParams).getColumn("t");
        assertThat(typeAnnotationTypes.size(), equalTo(1));
        assertThat(typeAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // field annotation
        Map<String, Object> fieldParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("f", "field").put("a", "a")
                .get();
        List<TypeDescriptor> fieldAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and f.name={f} and a.fqn={a} RETURN t",
                fieldParams).getColumn("t");
        assertThat(fieldAnnotationTypes.size(), equalTo(1));
        assertThat(fieldAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // method annotation
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> methodAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(methodAnnotationTypes.size(), equalTo(1));
        assertThat(methodAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // parameter annotation
        List<TypeDescriptor> parameterAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN t",
                params).getColumn("t");
        assertThat(parameterAnnotationTypes.size(), equalTo(1));
        assertThat(parameterAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveAnnotationType" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveAnnotationTypeUnique() throws Exception {
        scanClasses("a", AnnotationType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (:Class {name: 'DependentType'})-[:ANNOTATED_BY]->(t:Annotation), (:Artifact {fqn: 'a'})-[:CONTAINS]->(a:Annotation {name: 'AnnotationType'}) MERGE (t)-[r:OF_TYPE {prop: 'value', resolved: false}]->(a) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (:Field {name: 'field'})-[:ANNOTATED_BY]->(t:Annotation), (:Artifact {fqn: 'a'})-[:CONTAINS]->(a:Annotation {name: 'AnnotationType'}) MERGE (t)-[r:OF_TYPE]->(a) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("OF_TYPE", 14, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveAnnotationType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("OF_TYPE", 16, 4, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveValue".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveValue() throws Exception {
        scanAndApply("classpath:ResolveValue");
        store.beginTransaction();
        // type value
        Map<String, Object> typeParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> typeValues = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:HAS]->(:Value)-[:IS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN t",
                typeParams).getColumn("t");
        assertThat(typeValues.size(), equalTo(1));
        assertThat(typeValues, hasItems(typeDescriptor(ValueType.class)));
        // enum value
        Map<String, Object> enumParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<FieldDescriptor> enumValues = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:HAS]->(:Value)-[:IS{resolved:true}]->(f:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and a.fqn={a} RETURN f",
                enumParams).getColumn("f");
        assertThat(enumValues.size(), equalTo(1));
        assertThat(enumValues, hasItems(fieldDescriptor(EnumType.B)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveValue" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveValueUnique() throws Exception {
        scanClasses("a", ValueType.class, EnumType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (v:Value {name: 'classValue'}), (:Artifact {fqn: 'a'})-[:CONTAINS]->(t:Type {name: 'ValueType'}) MERGE (v)-[r:IS {prop: 'value', resolved: false}]->(t) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (v:Value {name: 'enumValue'}), (f:Field {name: 'B'}) MERGE (v)-[r:IS]->(f) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("IS", 4, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveValue").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("IS", 4, 2, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveReads".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveReads() throws Exception {
        scanAndApply("classpath:ResolveReads");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "fieldAccess").put("a", "a")
                .get();
        List<ReadsDescriptor> reads = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[r:READS{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN r",
                params).getColumn("r");
        assertThat(reads.size(), equalTo(1));
        ReadsDescriptor readsDescriptor = reads.get(0);
        assertThat(readsDescriptor.getLineNumber(), greaterThan(0));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveReads" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveReadsUniqueSameLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and correct line number
        assertThat(query("MATCH (m:Method {name: 'fieldAccess'}), (f:Field {name: 'foo'}) MERGE (m)-[r:READS {lineNumber: 17, prop: 'value', resolved: false}]->(f) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("READS", 2, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveReads").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("READS", 2, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveReads" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveReadsUniqueDifferentLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and different line number
        assertThat(query("MATCH (m:Method {name: 'fieldAccess'}), (f:Field {name: 'foo'}) MERGE (m)-[r:READS {lineNumber: 20, prop: 'value', resolved: false}]->(f) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("READS", 2, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveReads").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("READS", 3, 1, 1);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveReads" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveReadsUniqueWithoutLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and without line number
        assertThat(query("MATCH (m:Method {name: 'fieldAccess'}), (f:Field {name: 'foo'}) MERGE (m)-[r:READS {prop: 'value', resolved: false}]->(f) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("READS", 2, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveReads").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("READS", 3, 1, 1);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveWrites".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveWrites() throws Exception {
        scanAndApply("classpath:ResolveWrites");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "fieldAccess").put("a", "a")
                .get();
        List<WritesDescriptor> writes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[w:WRITES{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN w",
                params).getColumn("w");
        assertThat(writes.size(), equalTo(1));
        WritesDescriptor writesDescriptor = writes.get(0);
        assertThat(writesDescriptor.getLineNumber(), greaterThan(0));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveWrites" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveWritesUniqueSameLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and correct line number
        assertThat(query("MATCH (m:Method {name: 'fieldAccess'}), (f:Field {name: 'foo'}) MERGE (m)-[r:WRITES {lineNumber: 18, prop: 'value', resolved: false}]->(f) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("WRITES", 3, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveWrites").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("WRITES", 3, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveWrites" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveWritesUniqueDifferentLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and different line number
        assertThat(query("MATCH (m:Method {name: 'fieldAccess'}), (f:Field {name: 'foo'}) MERGE (m)-[r:WRITES {lineNumber: 10, prop: 'value', resolved: false}]->(f) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("WRITES", 3, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveWrites").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("WRITES", 4, 1, 1);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveWrites" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveWritesUniqueWithoutLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and without line number
        assertThat(query("MATCH (m:Method {name: 'fieldAccess'}), (f:Field {name: 'foo'}) MERGE (m)-[r:WRITES {prop: 'value', resolved: false}]->(f) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("WRITES", 3, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveWrites").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("WRITES", 4, 1, 1);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveInvokes".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveInvokes() throws Exception {
        scanAndApply("classpath:ResolveInvokes");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "methodInvocation")
                .put("a", "a").get();
        List<InvokesDescriptor> invocations = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[i:INVOKES{resolved:true}]->(:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn={dependentType} and m.name={m} and a.fqn={a} RETURN i",
                params).getColumn("i");
        assertThat(invocations.size(), equalTo(1));
        InvokesDescriptor invokesDescriptor = invocations.get(0);
        assertThat(invokesDescriptor.getLineNumber(), greaterThan(0));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveInvokes" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveInvokesUniqueSameLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and correct line number
        assertThat(query("MATCH (m1:Method {name: 'methodInvocation'}), (m2:Method {name: 'bar'}) MERGE (m1)-[r:INVOKES {lineNumber: 22, prop: 'value', resolved: false}]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("INVOKES", 4, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveInvokes").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("INVOKES", 5, 2, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveInvokes" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveInvokesUniqueDifferentLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and different line number
        assertThat(query("MATCH (m1:Method {name: 'methodInvocation'}), (m2:Method {name: 'bar'}) MERGE (m1)-[r:INVOKES {lineNumber: 10, prop: 'value', resolved: false}]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("INVOKES", 4, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveInvokes").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("INVOKES", 6, 2, 1);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "classpath:resolveInvokes" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveInvokesUniqueWithoutLine() throws Exception {
        scanClasses("a", ClassType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept("classpath:ResolveMember").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and without line number
        assertThat(query("MATCH (m1:Method {name: 'methodInvocation'}), (m2:Method {name: 'bar'}) MERGE (m1)-[r:INVOKES {prop: 'value', resolved: false}]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("INVOKES", 4, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("classpath:ResolveInvokes").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("INVOKES", 6, 2, 1);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:Resolve".
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolve() throws Exception {
        scanAndApply("classpath:Resolve");
        store.beginTransaction();
        List<String> concepts = query("MATCH (c:Concept) RETURN c.id as id").getColumn("id");
        assertThat(
                concepts,
                hasItems("classpath:ResolveDependency", "classpath:ResolveExtends", "classpath:ResolveImplements", "classpath:ResolveFieldType",
                        "classpath:ResolveThrows", "classpath:ResolveReturns", "classpath:ResolveParameterType", "classpath:ResolveAnnotationType",
                        "classpath:ResolveValue", "classpath:ResolveReads", "classpath:ResolveWrites", "classpath:ResolveInvokes"));
        store.commitTransaction();
    }

    private void scanAndApply(String concept) throws Exception {
        scanClasses("a", ClassType.class, InterfaceType.class, AnnotationType.class, EnumType.class, ExceptionType.class, ValueType.class);
        scanClasses("b", DependentType.class);
        assertThat(applyConcept(concept).getStatus(), equalTo(SUCCESS));
    }

    /**
     * Verifies a unique relation with property. An existing transaction is assumed.
     * @param relationName The name of the relation.
     * @param total The total of relations with the given name.
     */
    private void verifyUniqueRelation(String relationName, int total) {
    	assertThat(query("MATCH ()-[r:" + relationName + " {prop: 'value'}]->() RETURN r").getColumn("r").size(), equalTo(1));
    	assertThat(query("MATCH ()-[r:" + relationName + "]->() RETURN r").getColumn("r").size(), equalTo(total));
    }

    /**
     * Verifies a unique relation with resolved property. An existing transaction is assumed.
     * @param relationName The name of the relation.
     * @param total The total of relations with the given name.
     * @param resolvedPositive The number of resolved relations with positive property.
     * @param resolvedNegative The number of resolved relations with negative property.
     */
    private void verifyUniqueRelation(String relationName, int total, int resolvedPositive, int resolvedNegative) {
    	verifyUniqueRelation(relationName, total);
    	List<Object> column = query("MATCH ()-[r:" + relationName + " {resolved: true}]->() RETURN r").getColumn("r");
    	if (resolvedPositive == 0) {
    		assertNull(column);
    	} else {
    		assertThat(column.size(), equalTo(resolvedPositive));
    	}
    	column = query("MATCH ()-[r:" + relationName + " {resolved: false}]->() RETURN r").getColumn("r");
    	if (resolvedNegative == 0) {
    		assertNull(column);
    	} else {
    		assertThat(column.size(), equalTo(resolvedNegative));
    	}
    }
}
