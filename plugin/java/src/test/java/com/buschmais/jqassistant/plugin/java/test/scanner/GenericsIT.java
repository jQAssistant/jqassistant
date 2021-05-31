package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.dependson.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class GenericsIT extends AbstractJavaPluginIT {

    @Test
    public void genericType() throws NoSuchMethodException {
        scanClasses(GenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (g:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (g:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(GenericType.class)));
        store.commitTransaction();
    }

    @Test
    public void boundGenericType() throws NoSuchMethodException {
        scanClasses(BoundGenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (b:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (b:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(BoundGenericType.class)));
        store.commitTransaction();
    }

    @Test
    public void nestedGenericType() throws NoSuchMethodException {
        scanClasses(NestedGenericType.class);
        store.beginTransaction();
        assertThat(query("MATCH (n:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(NestedGenericType.class)));
        assertThat(query("MATCH (n:Type)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"),
                hasItem(typeDescriptor(GenericType.class)));
        store.commitTransaction();
    }

    @Test
    public void nestedGenericMethod() throws NoSuchMethodException {
        scanClasses(NestedGenericMethod.class);
        store.beginTransaction();
        assertThat(query("MATCH (n:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(NestedGenericMethod.class)));
        store.commitTransaction();
    }

    @Test
    public void extendsGenericClass() throws NoSuchMethodException {
        scanClasses(ExtendsGenericClass.class);
        store.beginTransaction();
        assertThat(query("MATCH (e:Type)-[:EXTENDS]->(s) RETURN s").getColumn("s"),
                hasItem(typeDescriptor(GenericType.class)));
        assertThat(query("MATCH (e:Type)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (e:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(ExtendsGenericClass.class)));
        store.commitTransaction();
    }

    @Test
    public void implementsGenericInterface() throws NoSuchMethodException {
        scanClasses(ImplementsGenericInterface.class);
        store.beginTransaction();
        assertThat(query("MATCH (igi:Type)-[:IMPLEMENTS]->(i) RETURN i").getColumn("i"), hasItem(typeDescriptor(Iterable.class)));
        assertThat(query("MATCH (igi:Type)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (igi:Type)-[:DECLARES]->(c:Constructor) RETURN c").getColumn("c"),
                hasItem(constructorDescriptor(ImplementsGenericInterface.class)));
        store.commitTransaction();
    }

    @Test
    public void genericMembers() {
        scanClasses(GenericMembers.class);
        store.beginTransaction();
        TestResult result = query("MATCH (gm:Type)-[:DEPENDS_ON]->(tv) RETURN tv");
        assertThat(result.getColumn("tv"), hasItem(typeDescriptor(Integer.class)));
        assertThat(result.getColumn("tv"), hasItem(typeDescriptor(Number.class)));
        assertThat(result.getColumn("tv"), hasItem(typeDescriptor(Double.class)));
        store.commitTransaction();
    }
}
