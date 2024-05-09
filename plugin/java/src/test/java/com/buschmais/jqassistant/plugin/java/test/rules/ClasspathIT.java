package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.*;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.b.DependentType;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

/**
 * Tests for the dependency concepts and result.
 */
class ClasspathIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java-classpath:resolveType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveType() throws Exception {
        scanClassesAndApply("java-classpath:ResolveType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("a1", "b").entry("a2", "a").build();
        List<TypeDescriptor> resolvedTypes = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(t1:Type)-[:RESOLVES_TO]->(rt:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn=$a1 and a2.fqn=$a2 RETURN rt",
                params).getColumn("rt");
        assertThat(resolvedTypes.size()).isEqualTo(6);
        assertThat(resolvedTypes, hasItems(typeDescriptor(AnnotationType.class), typeDescriptor(ClassType.class), typeDescriptor(InterfaceType.class),
                typeDescriptor(EnumType.class), typeDescriptor(ExceptionType.class), typeDescriptor(ValueType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveMember".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveMember() throws Exception {
        scanClassesAndApply("java-classpath:ResolveMember");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("a1", "b").entry("a2", "a").build();
        // Methods
        List<MethodDescriptor> resolvedMethods = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(:Type)-[:DECLARES]->()-[:RESOLVES_TO]->(rm:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn=$a1 and a2.fqn=$a2 RETURN rm",
                params).getColumn("rm");
        assertThat(resolvedMethods.size()).isEqualTo(2);
        assertThat(resolvedMethods, hasItems(constructorDescriptor(ClassType.class), methodDescriptor(ClassType.class, "bar", int.class)));
        // Fields
        List<FieldDescriptor> resolvedFields = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(:Type)-[:DECLARES]->()-[:RESOLVES_TO]->(rf:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn=$a1 and a2.fqn=$a2 RETURN rf",
                params).getColumn("rf");
        assertThat(resolvedFields.size()).isEqualTo(2);
        assertThat(resolvedFields, hasItems(fieldDescriptor(ClassType.class, "foo"), fieldDescriptor(EnumType.B)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveDependsOn".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveDependsOn() throws Exception {
        resolver("java-classpath:ResolveDependsOn");
    }

    private void resolver(String concept) throws Exception {
        scanClassesAndApply(concept);
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("a", "a").build();
        List<TypeDescriptor> dependencies = query(
                "MATCH (dependentType:Type)-[d:DEPENDS_ON{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a and d.weight is not null RETURN t",
                params).getColumn("t");
        assertThat(dependencies.size()).isEqualTo(6);
        assertThat(dependencies, hasItems(typeDescriptor(AnnotationType.class), typeDescriptor(ClassType.class), typeDescriptor(InterfaceType.class),
                typeDescriptor(EnumType.class), typeDescriptor(ExceptionType.class), typeDescriptor(ValueType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveExtends".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveExtends() throws Exception {
        scanClassesAndApply("java-classpath:ResolveExtends");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("a", "a").build();
        List<TypeDescriptor> extendedTypes = query(
                "MATCH (dependentType:Type)-[:EXTENDS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(extendedTypes.size()).isEqualTo(1);
        assertThat(extendedTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveImplements".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveImplements() throws Exception {
        scanClassesAndApply("java-classpath:ResolveImplements");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("a", "a").build();
        List<TypeDescriptor> implementedTypes = query(
                "MATCH (dependentType:Type)-[:IMPLEMENTS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(implementedTypes.size()).isEqualTo(1);
        assertThat(implementedTypes, hasItems(typeDescriptor(InterfaceType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveFieldType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveFieldType() throws Exception {
        scanClassesAndApply("java-classpath:ResolveFieldType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("f", "field").entry("a", "a").build();
        List<TypeDescriptor> fieldTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and f.name=$f and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(fieldTypes.size()).isEqualTo(1);
        assertThat(fieldTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveThrows".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveThrows() throws Exception {
        scanClassesAndApply("java-classpath:ResolveThrows");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "signature").entry("a", "a")
                .build();
        List<TypeDescriptor> exceptionTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:THROWS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(exceptionTypes.size()).isEqualTo(1);
        assertThat(exceptionTypes, hasItems(typeDescriptor(ExceptionType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveReturns".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveReturns() throws Exception {
        scanClassesAndApply("java-classpath:ResolveReturns");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "signature").entry("a", "a")
                .build();
        List<TypeDescriptor> returnTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:RETURNS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(returnTypes.size()).isEqualTo(1);
        assertThat(returnTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveParameterType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveParameterType() throws Exception {
        scanClassesAndApply("java-classpath:ResolveParameterType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "signature").entry("a", "a")
                .build();
        List<TypeDescriptor> parameterTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(parameterTypes.size()).isEqualTo(1);
        assertThat(parameterTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveAnnotationType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveAnnotationType() throws Exception {
        scanClassesAndApply("java-classpath:ResolveAnnotationType");
        store.beginTransaction();
        // type annotation
        Map<String, Object> typeParams = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("a", "a").build();
        List<TypeDescriptor> typeAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                typeParams).getColumn("t");
        assertThat(typeAnnotationTypes.size()).isEqualTo(1);
        assertThat(typeAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // field annotation
        Map<String, Object> fieldParams = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("f", "field").entry("a", "a")
                .build();
        List<TypeDescriptor> fieldAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and f.name=$f and a.fqn=$a RETURN t",
                fieldParams).getColumn("t");
        assertThat(fieldAnnotationTypes.size()).isEqualTo(1);
        assertThat(fieldAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // method annotation
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "signature").entry("a", "a")
                .build();
        List<TypeDescriptor> methodAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(methodAnnotationTypes.size()).isEqualTo(1);
        assertThat(methodAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // parameter annotation
        List<TypeDescriptor> parameterAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(parameterAnnotationTypes.size()).isEqualTo(1);
        assertThat(parameterAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveOfRawType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveOfRawType() throws Exception {
        scanClassesAndApply("java-classpath:ResolveOfRawType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("f", "genericType").entry("a", "a")
                .build();
        List<TypeDescriptor> parameterTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:OF_GENERIC_TYPE]->(:ParameterizedType)-[:HAS_ACTUAL_TYPE_ARGUMENT]->(:Bound)-[:OF_RAW_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and f.name=$f and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(parameterTypes.size()).isEqualTo(1);
        assertThat(parameterTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveValue".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveValue() throws Exception {
        scanClassesAndApply("java-classpath:ResolveValue");
        store.beginTransaction();
        // type value
        Map<String, Object> typeParams = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("a", "a").build();
        List<TypeDescriptor> typeValues = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:HAS]->(:Value)-[:IS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                typeParams).getColumn("t");
        assertThat(typeValues.size()).isEqualTo(1);
        assertThat(typeValues, hasItems(typeDescriptor(ValueType.class)));
        // enum value
        Map<String, Object> enumParams = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("a", "a").build();
        List<FieldDescriptor> enumValues = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:HAS]->(:Value)-[:IS{resolved:true}]->(f:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN f",
                enumParams).getColumn("f");
        assertThat(enumValues.size()).isEqualTo(1);
        assertThat(enumValues, hasItems(fieldDescriptor(EnumType.B)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveReads".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveReads() throws Exception {
        scanClassesAndApply("java-classpath:ResolveReads");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "fieldAccess").entry("a", "a")
                .build();
        List<ReadsDescriptor> reads = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[r:READS{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN r",
                params).getColumn("r");
        assertThat(reads.size()).isEqualTo(2);
        verifyAllLineNumbers(reads, greaterThan(0));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveReads".
     *
     * @throws IOException
     *             If the test fails.
     */

    @Test
    void resolveReadsWithoutLineNumber() throws Exception {
        scanClasses();
        store.beginTransaction();
        query("MATCH (:Method)-[r:READS]->(:Field) REMOVE r.lineNumber");
        store.commitTransaction();
        applyConcept("java-classpath:ResolveReads");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "fieldAccess").entry("a", "a")
                .build();
        List<ReadsDescriptor> reads = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[r:READS{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN r",
                params).getColumn("r");
        assertThat(reads.size()).isEqualTo(2);
        verifyAllLineNumbers(reads, nullValue());
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveWrites".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveWrites() throws Exception {
        scanClassesAndApply("java-classpath:ResolveWrites");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "fieldAccess").entry("a", "a")
                .build();
        List<WritesDescriptor> writes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[w:WRITES{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN w",
                params).getColumn("w");
        assertThat(writes.size()).isEqualTo(2);
        verifyAllLineNumbers(writes, greaterThan(0));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveWrites" without line number
     * information.
     *
     * @throws IOException
     *             If the test fails.
     */

    @Test
    void resolveWritesWithoutLineNumber() throws Exception {
        scanClasses();
        store.beginTransaction();
        query("MATCH (:Method)-[w:WRITES]->(:Field) REMOVE w.lineNumber");
        store.commitTransaction();
        applyConcept("java-classpath:ResolveWrites");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "fieldAccess").entry("a", "a")
                .build();
        List<WritesDescriptor> writes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[w:WRITES{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN w",
                params).getColumn("w");
        assertThat(writes.size()).isEqualTo(2);
        verifyAllLineNumbers(writes, nullValue());
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveInvokes".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveInvokes() throws Exception {
        scanClassesAndApply("java-classpath:ResolveInvokes");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "methodInvocation")
                .entry("a", "a").build();
        List<InvokesDescriptor> invocations = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[i:INVOKES{resolved:true}]->(:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN i",
                params).getColumn("i");
        assertThat(invocations.size()).isEqualTo(2);
        verifyAllLineNumbers(invocations, greaterThan(0));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java-classpath:resolveInvokes" without line number
     * information.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolveInvokesWithoutLineNumber() throws Exception {
        scanClasses();
        store.beginTransaction();
        query("MATCH (:Method)-[i:INVOKES]->(:Method) REMOVE i.lineNumber");
        store.commitTransaction();
        applyConcept("java-classpath:ResolveInvokes");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("dependentType", DependentType.class.getName()).entry("m", "methodInvocation")
                .entry("a", "a").build();
        List<InvokesDescriptor> invocations = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[i:INVOKES{resolved:true}]->(:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN i",
                params).getColumn("i");
        assertThat(invocations.size()).isEqualTo(2);
        verifyAllLineNumbers(invocations, nullValue());
        store.commitTransaction();
    }

    private void verifyAllLineNumbers(List<? extends LineNumberDescriptor> lineNumberDescriptors, Matcher<? super Integer> lineNumberMatcher) {
        for (LineNumberDescriptor lineNumberDescriptor : lineNumberDescriptors) {
            assertThat(lineNumberDescriptor.getLineNumber(), lineNumberMatcher);
        }
    }

    /**
     * Verifies the concept "java-classpath:Resolve".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void resolve() throws Exception {
        scanClassesAndApply("java-classpath:Resolve");
        store.beginTransaction();
        List<String> concepts = query("MATCH (c:Concept) RETURN c.id as id").getColumn("id");
        assertThat(concepts,
                hasItems("java-classpath:ResolveDependsOn", "java-classpath:ResolveExtends", "java-classpath:ResolveImplements", "java-classpath:ResolveFieldType",
                        "java-classpath:ResolveThrows", "java-classpath:ResolveReturns", "java-classpath:ResolveParameterType", "java-classpath:ResolveAnnotationType",
                        "java-classpath:ResolveValue", "java-classpath:ResolveReads", "java-classpath:ResolveWrites", "java-classpath:ResolveInvokes"));
        store.commitTransaction();
    }

    private void scanClassesAndApply(String concept) throws Exception {
        scanClasses();
        assertThat(applyConcept(concept).getStatus()).isEqualTo(SUCCESS);
    }

    private void scanClasses() {
        scanClasses("a", ClassType.class, InterfaceType.class, AnnotationType.class, EnumType.class, ExceptionType.class, ValueType.class);
        scanClasses("b", DependentType.class);
    }

}
