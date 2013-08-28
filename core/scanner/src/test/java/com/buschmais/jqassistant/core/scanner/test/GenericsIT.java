package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.scanner.test.set.generics.*;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class GenericsIT extends AbstractScannerIT {

    @Test
    public void genericType() throws IOException, NoSuchMethodException {
        scanClasses(GenericType.class);
        assertThat(query("MATCH (g:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (g:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(GenericType.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void boundGenericType() throws IOException, NoSuchMethodException {
        scanClasses(BoundGenericType.class);
        assertThat(query("MATCH (b:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (b:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(BoundGenericType.class)));
        assertThat(query("MATCH (b:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void nestedGenericType() throws IOException, NoSuchMethodException {
        scanClasses(NestedGenericType.class);
        assertThat(query("MATCH (n:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(NestedGenericType.class)));
        assertThat(query("MATCH (n:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(GenericType.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void nestedGenericMethod() throws IOException, NoSuchMethodException {
        scanClasses(NestedGenericMethod.class);
        assertThat(query("MATCH (n:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(query("MATCH (n:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(NestedGenericMethod.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
        assertThat(query("MATCH (m:METHOD)-[:DEPENDS_ON]->(d) WHERE NOT m:CONSTRUCTOR RETURN d").getColumns().get("d"), hasItem(typeDescriptor(GenericType.class)));
    }

    @Test
    public void extendsGenericClass() throws IOException, NoSuchMethodException {
        scanClasses(ExtendsGenericClass.class);
        assertThat(query("MATCH (e:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(GenericType.class)));
        assertThat(query("MATCH (e:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (e:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(ExtendsGenericClass.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void implementsGenericInterface() throws IOException, NoSuchMethodException {
        scanClasses(ImplementsGenericInterface.class);
        assertThat(query("MATCH (igi:TYPE)-[:IMPLEMENTS]->(i) RETURN i").getColumns().get("i"), hasItem(typeDescriptor(Iterable.class)));
        assertThat(query("MATCH (igi:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(query("MATCH (igi:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(ImplementsGenericInterface.class)));
        assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }
}
