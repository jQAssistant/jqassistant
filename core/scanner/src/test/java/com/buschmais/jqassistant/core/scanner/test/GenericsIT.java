package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.test.set.generics.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class GenericsIT extends AbstractScannerIT {

    @Test
    public void genericType() throws IOException, NoSuchMethodException {
        scanClasses(GenericType.class);
        assertThat(executeQuery("MATCH (g:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(executeQuery("MATCH (g:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(GenericType.class)));
        assertThat(executeQuery("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void boundGenericType() throws IOException, NoSuchMethodException {
        scanClasses(BoundGenericType.class);
        assertThat(executeQuery("MATCH (b:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(executeQuery("MATCH (b:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(BoundGenericType.class)));
        assertThat(executeQuery("MATCH (b:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(executeQuery("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void nestedGenericType() throws IOException, NoSuchMethodException {
        scanClasses(NestedGenericType.class);
        assertThat(executeQuery("MATCH (n:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(executeQuery("MATCH (n:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(NestedGenericType.class)));
        assertThat(executeQuery("MATCH (n:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(GenericType.class)));
        assertThat(executeQuery("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void nestedGenericMethod() throws IOException, NoSuchMethodException {
        scanClasses(NestedGenericMethod.class);
        assertThat(executeQuery("MATCH (n:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(Object.class)));
        assertThat(executeQuery("MATCH (n:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(NestedGenericMethod.class)));
        assertThat(executeQuery("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
        assertThat(executeQuery("MATCH (m:METHOD)-[:DEPENDS_ON]->(d) WHERE NOT m:CONSTRUCTOR RETURN d").getColumns().get("d"), hasItem(typeDescriptor(GenericType.class)));
    }

    @Test
    public void extendsGenericClass() throws IOException, NoSuchMethodException {
        scanClasses(ExtendsGenericClass.class);
        assertThat(executeQuery("MATCH (e:TYPE)-[:EXTENDS]->(s) RETURN s").getColumns().get("s"), hasItem(typeDescriptor(GenericType.class)));
        assertThat(executeQuery("MATCH (e:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(executeQuery("MATCH (e:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(ExtendsGenericClass.class)));
        assertThat(executeQuery("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }

    @Test
    public void implementsGenericInterface() throws IOException, NoSuchMethodException {
        scanClasses(ImplementsGenericInterface.class);
        assertThat(executeQuery("MATCH (igi:TYPE)-[:IMPLEMENTS]->(i) RETURN i").getColumns().get("i"), hasItem(typeDescriptor(Iterable.class)));
        assertThat(executeQuery("MATCH (igi:TYPE)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(Number.class)));
        assertThat(executeQuery("MATCH (igi:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(ImplementsGenericInterface.class)));
        assertThat(executeQuery("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumns().get("d"), hasItem(typeDescriptor(void.class)));
    }
}
