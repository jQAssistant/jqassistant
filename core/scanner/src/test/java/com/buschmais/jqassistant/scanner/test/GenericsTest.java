package com.buschmais.jqassistant.scanner.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.scanner.test.sets.generics.BoundGenericType;
import com.buschmais.jqassistant.scanner.test.sets.generics.ExtendsGenericClass;
import com.buschmais.jqassistant.scanner.test.sets.generics.GenericType;
import com.buschmais.jqassistant.scanner.test.sets.generics.ImplementsGenericInterface;
import com.buschmais.jqassistant.scanner.test.sets.generics.NestedGenericMethod;
import com.buschmais.jqassistant.scanner.test.sets.generics.NestedGenericType;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;

public class GenericsTest extends AbstractScannerTest {

	@Test
	public void genericType() throws IOException {
		ClassDescriptor genericType = stubClass(GenericType.class);
		MethodDescriptor constructor = new MethodDescriptor();
		when(store.resolveMethodDescriptor(genericType, "void <init>()"))
				.thenReturn(constructor);

		scanner.scanClass(GenericType.class);

		assertThat(genericType.getSuperClass(), equalTo(javaLangObject));
		assertThat(genericType.getContains(), hasItem(constructor));
		assertThat(genericType.getDependencies(), hasItem(_void));
	}

	@Test
	public void boundGenericType() throws IOException {
		ClassDescriptor boundGenericType = stubClass(BoundGenericType.class);
		ClassDescriptor javaLangNumber = stubClass(Number.class);
		MethodDescriptor constructor = new MethodDescriptor();
		when(store.resolveMethodDescriptor(boundGenericType, "void <init>()"))
				.thenReturn(constructor);

		scanner.scanClass(BoundGenericType.class);

		assertThat(boundGenericType.getSuperClass(), equalTo(javaLangObject));
		assertThat(boundGenericType.getContains(), hasItem(constructor));
		assertThat(boundGenericType.getDependencies(), hasItem(javaLangNumber));
		assertThat(constructor.getDependencies(), hasItem(_void));
	}

	@Test
	public void nestedGenericType() throws IOException {
		ClassDescriptor nestedGenericType = stubClass(NestedGenericType.class);
		ClassDescriptor genericType = stubClass(GenericType.class);
		MethodDescriptor constructor = new MethodDescriptor();
		when(store.resolveMethodDescriptor(nestedGenericType, "void <init>()"))
				.thenReturn(constructor);

		scanner.scanClass(NestedGenericType.class);

		assertThat(nestedGenericType.getSuperClass(), equalTo(javaLangObject));
		assertThat(nestedGenericType.getContains(), hasItem(constructor));
		assertThat(nestedGenericType.getDependencies(), hasItem(genericType));
		assertThat(constructor.getDependencies(), hasItem(_void));
	}

	@Test
	public void nestedGenericMethod() throws IOException {
		ClassDescriptor nestedGenericType = stubClass(NestedGenericMethod.class);
		ClassDescriptor genericType = stubClass(GenericType.class);
		MethodDescriptor constructor = new MethodDescriptor();
		when(store.resolveMethodDescriptor(nestedGenericType, "void <init>()"))
				.thenReturn(constructor);
		MethodDescriptor get = new MethodDescriptor();
		when(
				store.resolveMethodDescriptor(
						nestedGenericType,
						"java.lang.Object get(com.buschmais.jqassistant.scanner.test.sets.generics.GenericType)"))
				.thenReturn(get);

		scanner.scanClass(NestedGenericMethod.class);

		assertThat(nestedGenericType.getSuperClass(), equalTo(javaLangObject));
		assertThat(nestedGenericType.getContains(), hasItem(constructor));
		assertThat(constructor.getDependencies(), hasItem(_void));
		assertThat(get.getDependencies(), hasItem(genericType));
	}

	@Test
	public void extendsGenericClass() throws IOException {
		ClassDescriptor extendsGenericClass = stubClass(ExtendsGenericClass.class);
		ClassDescriptor genericType = stubClass(GenericType.class);
		ClassDescriptor javaLangNumber = stubClass(Number.class);
		MethodDescriptor constructor = new MethodDescriptor();
		when(
				store.resolveMethodDescriptor(extendsGenericClass,
						"void <init>()")).thenReturn(constructor);

		scanner.scanClass(ExtendsGenericClass.class);

		assertThat(extendsGenericClass.getSuperClass(), equalTo(genericType));
		assertThat(extendsGenericClass.getDependencies(),
				hasItem(javaLangNumber));
		assertThat(extendsGenericClass.getContains(), hasItem(constructor));
		assertThat(constructor.getDependencies(), hasItem(_void));
	}

	@Test
	public void implementsGenericInterface() throws IOException {
		ClassDescriptor extendsGenericClass = stubClass(ImplementsGenericInterface.class);
		ClassDescriptor javaUtilIterable = stubClass(Iterable.class);
		ClassDescriptor javaLangNumber = stubClass(Number.class);
		MethodDescriptor constructor = new MethodDescriptor();
		when(
				store.resolveMethodDescriptor(extendsGenericClass,
						"void <init>()")).thenReturn(constructor);

		scanner.scanClass(ImplementsGenericInterface.class);

		assertThat(extendsGenericClass.getInterfaces(),
				hasItem(javaUtilIterable));
		assertThat(extendsGenericClass.getDependencies(),
				hasItem(javaLangNumber));
		assertThat(extendsGenericClass.getContains(), hasItem(constructor));
		assertThat(constructor.getDependencies(), hasItem(_void));
	}
}
