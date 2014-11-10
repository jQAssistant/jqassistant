package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericMembers;

public class GenericsIT extends AbstractJavaPluginIT {

    @Test
    public void genericType() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (g:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (g:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
        store.commitTransaction();
    }

    @Test
    public void boundGenericType() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.BoundGenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (b:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (b:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.BoundGenericType.class)));
        store.commitTransaction();
    }

    @Test
    public void nestedGenericType() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (n:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericType.class)));
        assertThat(query("MATCH (n:Type)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"),
                hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
    }

    @Test
    public void nestedGenericMethod() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericMethod.class);
        store.beginTransaction();
        assertThat(query("MATCH (n:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.NestedGenericMethod.class)));
        store.commitTransaction();
    }

    @Test
    public void extendsGenericClass() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ExtendsGenericClass.class);
        store.beginTransaction();
        assertThat(query("MATCH (e:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"),
                hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
        assertThat(query("MATCH (e:Type)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (e:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ExtendsGenericClass.class)));
        store.commitTransaction();
    }

    @Test
    public void implementsGenericInterface() throws IOException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ImplementsGenericInterface.class);
        store.beginTransaction();
        assertThat(query("MATCH (igi:Type)-[:IMPLEMENTS]->(i) RETURN i").getColumn("i"), hasItem(typeDescriptor(Iterable.class)));
        assertThat(query("MATCH (igi:Type)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (igi:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ImplementsGenericInterface.class)));
        store.commitTransaction();
    }

    @Test
    public void genericMembers() throws IOException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(GenericMembers.class);
        store.beginTransaction();
        TestResult result = query("MATCH (gm:Type)-[:DEPENDS_ON]->(tv) RETURN tv");
        assertThat(result.getColumn("tv"), hasItem(typeDescriptor(Integer.class)));
        assertThat(result.getColumn("tv"), hasItem(typeDescriptor(Number.class)));
        assertThat(result.getColumn("tv"), hasItem(typeDescriptor(Double.class)));
        store.commitTransaction();
    }
}
