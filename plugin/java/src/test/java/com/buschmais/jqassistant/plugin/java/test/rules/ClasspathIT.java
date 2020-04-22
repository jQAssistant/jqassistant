package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Tests for the dependency concepts and result.
 */
public class ClasspathIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "classpath:resolveType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveType() throws Exception {
        scanClassesAndApply("classpath:ResolveType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("a1", "b").put("a2", "a").get();
        List<TypeDescriptor> resolvedTypes = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(t1:Type)-[:RESOLVES_TO]->(rt:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn=$a1 and a2.fqn=$a2 RETURN rt",
                params).getColumn("rt");
        assertThat(resolvedTypes.size(), equalTo(6));
        assertThat(resolvedTypes, hasItems(typeDescriptor(AnnotationType.class), typeDescriptor(ClassType.class), typeDescriptor(InterfaceType.class),
                typeDescriptor(EnumType.class), typeDescriptor(ExceptionType.class), typeDescriptor(ValueType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveMember".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveMember() throws Exception {
        scanClassesAndApply("classpath:ResolveMember");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("a1", "b").put("a2", "a").get();
        // Methods
        List<MethodDescriptor> resolvedMethods = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(:Type)-[:DECLARES]->()-[:RESOLVES_TO]->(rm:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn=$a1 and a2.fqn=$a2 RETURN rm",
                params).getColumn("rm");
        assertThat(resolvedMethods.size(), equalTo(2));
        assertThat(resolvedMethods, hasItems(constructorDescriptor(ClassType.class), methodDescriptor(ClassType.class, "bar", int.class)));
        // Fields
        List<FieldDescriptor> resolvedFields = query(
                "MATCH (a1:Artifact)-[:REQUIRES]->(:Type)-[:DECLARES]->()-[:RESOLVES_TO]->(rf:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a2:Artifact) WHERE a1.fqn=$a1 and a2.fqn=$a2 RETURN rf",
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
     */
    @Test
    public void resolveDependency() throws Exception {
        resolver("classpath:ResolveDependency");
    }

    /**
     * Verifies the concept "classpath:resolveDependsOn".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveDependsOn() throws Exception {
        resolver("classpath:ResolveDependsOn");
    }

    private void resolver(String concept) throws Exception {
        scanClassesAndApply(concept);
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> dependencies = query(
                "MATCH (dependentType:Type)-[d:DEPENDS_ON{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a and exists(d.weight) RETURN t",
                params).getColumn("t");
        assertThat(dependencies.size(), equalTo(6));
        assertThat(dependencies, hasItems(typeDescriptor(AnnotationType.class), typeDescriptor(ClassType.class), typeDescriptor(InterfaceType.class),
                typeDescriptor(EnumType.class), typeDescriptor(ExceptionType.class), typeDescriptor(ValueType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveExtends".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveExtends() throws Exception {
        scanClassesAndApply("classpath:ResolveExtends");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> extendedTypes = query(
                "MATCH (dependentType:Type)-[:EXTENDS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(extendedTypes.size(), equalTo(1));
        assertThat(extendedTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveImplements".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveImplements() throws Exception {
        scanClassesAndApply("classpath:ResolveImplements");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> implementedTypes = query(
                "MATCH (dependentType:Type)-[:IMPLEMENTS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(implementedTypes.size(), equalTo(1));
        assertThat(implementedTypes, hasItems(typeDescriptor(InterfaceType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveFieldType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveFieldType() throws Exception {
        scanClassesAndApply("classpath:ResolveFieldType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("f", "field").put("a", "a").get();
        List<TypeDescriptor> fieldTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and f.name=$f and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(fieldTypes.size(), equalTo(1));
        assertThat(fieldTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveThrows".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveThrows() throws Exception {
        scanClassesAndApply("classpath:ResolveThrows");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> exceptionTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:THROWS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(exceptionTypes.size(), equalTo(1));
        assertThat(exceptionTypes, hasItems(typeDescriptor(ExceptionType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveReturns".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveReturns() throws Exception {
        scanClassesAndApply("classpath:ResolveReturns");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> returnTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:RETURNS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(returnTypes.size(), equalTo(1));
        assertThat(returnTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveParameterType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveParameterType() throws Exception {
        scanClassesAndApply("classpath:ResolveParameterType");
        store.beginTransaction();
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> parameterTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(parameterTypes.size(), equalTo(1));
        assertThat(parameterTypes, hasItems(typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveAnnotationType".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveAnnotationType() throws Exception {
        scanClassesAndApply("classpath:ResolveAnnotationType");
        store.beginTransaction();
        // type annotation
        Map<String, Object> typeParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> typeAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                typeParams).getColumn("t");
        assertThat(typeAnnotationTypes.size(), equalTo(1));
        assertThat(typeAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // field annotation
        Map<String, Object> fieldParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("f", "field").put("a", "a")
                .get();
        List<TypeDescriptor> fieldAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(f:Field)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and f.name=$f and a.fqn=$a RETURN t",
                fieldParams).getColumn("t");
        assertThat(fieldAnnotationTypes.size(), equalTo(1));
        assertThat(fieldAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // method annotation
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "signature").put("a", "a")
                .get();
        List<TypeDescriptor> methodAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(methodAnnotationTypes.size(), equalTo(1));
        assertThat(methodAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        // parameter annotation
        List<TypeDescriptor> parameterAnnotationTypes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:ANNOTATED_BY]->()-[:OF_TYPE{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN t",
                params).getColumn("t");
        assertThat(parameterAnnotationTypes.size(), equalTo(1));
        assertThat(parameterAnnotationTypes, hasItems(typeDescriptor(AnnotationType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveValue".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveValue() throws Exception {
        scanClassesAndApply("classpath:ResolveValue");
        store.beginTransaction();
        // type value
        Map<String, Object> typeParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<TypeDescriptor> typeValues = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:HAS]->(:Value)-[:IS{resolved:true}]->(t:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN t",
                typeParams).getColumn("t");
        assertThat(typeValues.size(), equalTo(1));
        assertThat(typeValues, hasItems(typeDescriptor(ValueType.class)));
        // enum value
        Map<String, Object> enumParams = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("a", "a").get();
        List<FieldDescriptor> enumValues = query(
                "MATCH (dependentType:Type)-[:ANNOTATED_BY]->()-[:HAS]->(:Value)-[:IS{resolved:true}]->(f:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and a.fqn=$a RETURN f",
                enumParams).getColumn("f");
        assertThat(enumValues.size(), equalTo(1));
        assertThat(enumValues, hasItems(fieldDescriptor(EnumType.B)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveReads".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveReads() throws Exception {
        scanClassesAndApply("classpath:ResolveReads");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "fieldAccess").put("a", "a")
                .get();
        List<ReadsDescriptor> reads = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[r:READS{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN r",
                params).getColumn("r");
        assertThat(reads.size(), equalTo(2));
        verifyAllLineNumbers(reads, greaterThan(0));
        store.commitTransaction();
    }
    /**
     * Verifies the concept "classpath:resolveReads".
     *
     * @throws IOException
     *             If the test fails.
     */

    @Test
    public void resolveReadsWithoutLineNumber() throws Exception {
        scanClasses();
        store.beginTransaction();
        query("MATCH (:Method)-[r:READS]->(:Field) REMOVE r.lineNumber");
        store.commitTransaction();
        applyConcept("classpath:ResolveReads");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "fieldAccess").put("a", "a")
                .get();
        List<ReadsDescriptor> reads = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[r:READS{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN r",
                params).getColumn("r");
        assertThat(reads.size(), equalTo(2));
        verifyAllLineNumbers(reads, nullValue());
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveWrites".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveWrites() throws Exception {
        scanClassesAndApply("classpath:ResolveWrites");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "fieldAccess").put("a", "a")
                .get();
        List<WritesDescriptor> writes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[w:WRITES{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN w",
                params).getColumn("w");
        assertThat(writes.size(), equalTo(2));
        verifyAllLineNumbers(writes, greaterThan(0));
        store.commitTransaction();
    }
    /**
     * Verifies the concept "classpath:resolveWrites" without line number information.
     *
     * @throws IOException
     *             If the test fails.
     */

    @Test
    public void resolveWritesWithoutLineNumber() throws Exception {
        scanClasses();
        store.beginTransaction();
        query("MATCH (:Method)-[w:WRITES]->(:Field) REMOVE w.lineNumber");
        store.commitTransaction();
        applyConcept("classpath:ResolveWrites");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "fieldAccess").put("a", "a")
                .get();
        List<WritesDescriptor> writes = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[w:WRITES{resolved:true}]->(:Field)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN w",
                params).getColumn("w");
        assertThat(writes.size(), equalTo(2));
        verifyAllLineNumbers(writes, nullValue());
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveInvokes".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveInvokes() throws Exception {
        scanClassesAndApply("classpath:ResolveInvokes");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "methodInvocation")
                .put("a", "a").get();
        List<InvokesDescriptor> invocations = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[i:INVOKES{resolved:true}]->(:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN i",
                params).getColumn("i");
        assertThat(invocations.size(), equalTo(2));
        verifyAllLineNumbers(invocations, greaterThan(0));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "classpath:resolveInvokes" without line number information.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolveInvokesWithoutLineNumber() throws Exception {
        scanClasses();
        store.beginTransaction();
        query("MATCH (:Method)-[i:INVOKES]->(:Method) REMOVE i.lineNumber");
        store.commitTransaction();
        applyConcept("classpath:ResolveInvokes");
        store.beginTransaction();
        // type value
        Map<String, Object> params = MapBuilder.<String, Object> create("dependentType", DependentType.class.getName()).put("m", "methodInvocation")
                .put("a", "a").get();
        List<InvokesDescriptor> invocations = query(
                "MATCH (dependentType:Type)-[:DECLARES]->(m:Method)-[i:INVOKES{resolved:true}]->(:Method)<-[:DECLARES]-(:Type)<-[:CONTAINS]-(a) WHERE dependentType.fqn=$dependentType and m.name=$m and a.fqn=$a RETURN i",
                params).getColumn("i");
        assertThat(invocations.size(), equalTo(2));
        verifyAllLineNumbers(invocations, nullValue());
        store.commitTransaction();
    }

    private void verifyAllLineNumbers(List<? extends LineNumberDescriptor> lineNumberDescriptors, Matcher<? super Integer> lineNumberMatcher) {
        for (LineNumberDescriptor lineNumberDescriptor: lineNumberDescriptors) {
            assertThat(lineNumberDescriptor.getLineNumber(), lineNumberMatcher);
        }
    }

    /**
     * Verifies the concept "classpath:Resolve".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void resolve() throws Exception {
        scanClassesAndApply("classpath:Resolve");
        store.beginTransaction();
        List<String> concepts = query("MATCH (c:Concept) RETURN c.id as id").getColumn("id");
        assertThat(concepts,
                hasItems("classpath:ResolveDependsOn", "classpath:ResolveExtends", "classpath:ResolveImplements", "classpath:ResolveFieldType",
                        "classpath:ResolveThrows", "classpath:ResolveReturns", "classpath:ResolveParameterType", "classpath:ResolveAnnotationType",
                        "classpath:ResolveValue", "classpath:ResolveReads", "classpath:ResolveWrites", "classpath:ResolveInvokes"));
        store.commitTransaction();
    }

    private void scanClassesAndApply(String concept) throws Exception {
        scanClasses();
        assertThat(applyConcept(concept).getStatus(), equalTo(SUCCESS));
    }

    private void scanClasses() {
        scanClasses("a", ClassType.class, InterfaceType.class, AnnotationType.class, EnumType.class, ExceptionType.class, ValueType.class);
        scanClasses("b", DependentType.class);
    }

}
