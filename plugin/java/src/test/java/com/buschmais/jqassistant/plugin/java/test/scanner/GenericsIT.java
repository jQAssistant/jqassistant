package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericMembers;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class GenericsIT extends AbstractPluginIT {

    @Test
    public void genericType() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (g:TYPE)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (g:TYPE)-[:DECLARES]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        store.commitTransaction();
    }

    @Test
    public void boundGenericType() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.BoundGenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (b:TYPE)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (b:TYPE)-[:DECLARES]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.BoundGenericType.class)));
        assertThat(query("MATCH (b:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        store.commitTransaction();
    }

    @Test
    public void nestedGenericType() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (n:TYPE)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:TYPE)-[:DECLARES]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericType.class)));
        assertThat(query("MATCH (n:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"),
                hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        store.commitTransaction();
    }

    @Test
    public void nestedGenericMethod() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericMethod.class);
        store.beginTransaction();
        assertThat(query("MATCH (n:TYPE)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:TYPE)-[:DECLARES]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericMethod.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        assertThat(query("MATCH (m:METHOD)-[:DEPENDS_ON]->(d) WHERE NOT m:CONSTRUCTOR RETURN d").getColumn("d"),
                hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
        store.commitTransaction();
    }

    @Test
    public void extendsGenericClass() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ExtendsGenericClass.class);
        store.beginTransaction();
        assertThat(query("MATCH (e:TYPE)-[:EXTENDS]->(s) RETURN s").getColumn("s"),
                hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
        assertThat(query("MATCH (e:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (e:TYPE)-[:DECLARES]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ExtendsGenericClass.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        store.commitTransaction();
    }

    @Test
    public void implementsGenericInterface() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ImplementsGenericInterface.class);
        store.beginTransaction();
        assertThat(query("MATCH (igi:TYPE)-[:IMPLEMENTS]->(i) RETURN i").getColumn("i"), hasItem(typeDescriptor(Iterable.class)));
        assertThat(query("MATCH (igi:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(
                query("MATCH (igi:TYPE)-[:DECLARES]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ImplementsGenericInterface.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        store.commitTransaction();
    }

    @Test
    public void genericMembers() throws IOException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(GenericMembers.class);
        store.beginTransaction();
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        TestResult result = query("MATCH (gm:TYPE)-[:DECLARES]->(f:FIELD), (f)-[:OF_TYPE]->(gt), (f)-[:DEPENDS_ON]->(tv) RETURN f, gt , tv");
        assertThat(result.getColumn("f"), hasItem(fieldDescriptor(GenericMembers.class, "integerList")));
        assertThat(result.getColumn("gt"), hasItem(typeDescriptor(List.class)));
        assertThat(result.getColumn("tv"), hasItem(typeDescriptor(Integer.class)));
        result = query("MATCH (gm:TYPE)-[:DECLARES]->(m:METHOD), (m)-[:RETURNS]->(rt), (m)-[:DEPENDS_ON]->(rtv) RETURN m, rt , rtv");
        assertThat(result.getColumn("m"), hasItem(methodDescriptor(GenericMembers.class, "get", List.class)));
        assertThat(result.getColumn("rt"), hasItem(typeDescriptor(Set.class)));
        assertThat(result.getColumn("rtv"), hasItem(typeDescriptor(Number.class)));
        result = query("MATCH (gm:TYPE)-[:DECLARES]->(m:METHOD), (m)-[:HAS]->(p), (p)-[:OF_TYPE]->(pt) ,(p)-[:DEPENDS_ON]->(ptv) RETURN pt , ptv");
        assertThat(result.getColumn("pt"), hasItem(typeDescriptor(List.class)));
        assertThat(result.getColumn("ptv"), hasItem(typeDescriptor(Double.class)));
        store.commitTransaction();
    }
}
