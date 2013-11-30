package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class GenericsIT extends AbstractPluginIT {

	@Test
	public void genericType() throws IOException, NoSuchMethodException {
		scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class);
        store.beginTransaction();
		assertThat(query("MATCH (g:TYPE)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
		assertThat(query("MATCH (g:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
				hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericType.class)));
		assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        store.commitTransaction();
	}

	@Test
	public void boundGenericType() throws IOException, NoSuchMethodException {
		scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.BoundGenericType.class);
        store.beginTransaction();
		assertThat(query("MATCH (b:TYPE)-[:EXTENDS]->(s) RETURN s").getColumn("s"), hasItem(typeDescriptor(Object.class)));
		assertThat(query("MATCH (b:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
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
		assertThat(query("MATCH (n:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
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
		assertThat(query("MATCH (n:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
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
		assertThat(query("MATCH (e:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
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
				query("MATCH (igi:TYPE)-[:CONTAINS]->(c:CONSTRUCTOR) RETURN c").getColumn("c"),
				hasItem(constructorDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ImplementsGenericInterface.class)));
		assertThat(query("MATCH (c:CONSTRUCTOR)-[:DEPENDS_ON]->(d) RETURN d").getColumn("d"), hasItem(typeDescriptor(void.class)));
        store.commitTransaction();
	}
}
