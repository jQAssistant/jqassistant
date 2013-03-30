package com.buschmais.jqassistant.scanner.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
		MethodDescriptor constructor = mock(MethodDescriptor.class);
		when(store.resolveMethodDescriptor(genericType, "void <init>()"))
				.thenReturn(constructor);

		scanner.scanClass(GenericType.class);

		verify(genericType).addSuperClass(javaLangObject);
		verify(genericType).addChild(constructor);
		verify(constructor).addDependency(_void);
	}

	@Test
	public void boundGenericType() throws IOException {
		ClassDescriptor boundGenericType = stubClass(BoundGenericType.class);
		ClassDescriptor javaLangNumber = stubClass(Number.class);
		MethodDescriptor constructor = mock(MethodDescriptor.class);
		when(store.resolveMethodDescriptor(boundGenericType, "void <init>()"))
				.thenReturn(constructor);

		scanner.scanClass(BoundGenericType.class);

		verify(boundGenericType).addSuperClass(javaLangObject);
		verify(boundGenericType).addChild(constructor);
		verify(boundGenericType).addDependency(javaLangNumber);
		verify(constructor).addDependency(_void);
	}

	@Test
	public void nestedGenericType() throws IOException {
		ClassDescriptor nestedGenericType = stubClass(NestedGenericType.class);
		ClassDescriptor genericType = stubClass(GenericType.class);
		MethodDescriptor constructor = mock(MethodDescriptor.class);
		when(store.resolveMethodDescriptor(nestedGenericType, "void <init>()"))
				.thenReturn(constructor);

		scanner.scanClass(NestedGenericType.class);

		verify(nestedGenericType).addSuperClass(javaLangObject);
		verify(nestedGenericType).addChild(constructor);
		verify(nestedGenericType).addDependency(genericType);
		verify(constructor).addDependency(_void);
	}

	@Test
	public void nestedGenericMethod() throws IOException {
		ClassDescriptor nestedGenericType = stubClass(NestedGenericMethod.class);
		ClassDescriptor genericType = stubClass(GenericType.class);
		MethodDescriptor constructor = mock(MethodDescriptor.class);
		when(store.resolveMethodDescriptor(nestedGenericType, "void <init>()"))
				.thenReturn(constructor);
		MethodDescriptor get = mock(MethodDescriptor.class);
		when(
				store.resolveMethodDescriptor(
						nestedGenericType,
						"java.lang.Object get(com.buschmais.jqassistant.scanner.test.sets.generics.GenericType)"))
				.thenReturn(get);

		scanner.scanClass(NestedGenericMethod.class);

		verify(nestedGenericType).addSuperClass(javaLangObject);
		verify(nestedGenericType).addChild(constructor);
		verify(constructor).addDependency(_void);
		verify(get).addDependency(genericType);
	}

	@Test
	public void extendsGenericClass() throws IOException {
		ClassDescriptor extendsGenericClass = stubClass(ExtendsGenericClass.class);
		ClassDescriptor genericType = stubClass(GenericType.class);
		ClassDescriptor javaLangNumber = stubClass(Number.class);
		MethodDescriptor constructor = mock(MethodDescriptor.class);
		when(
				store.resolveMethodDescriptor(extendsGenericClass,
						"void <init>()")).thenReturn(constructor);

		scanner.scanClass(ExtendsGenericClass.class);

		verify(extendsGenericClass).addSuperClass(genericType);
		verify(extendsGenericClass).addDependency(javaLangNumber);
		verify(extendsGenericClass).addChild(constructor);
		verify(constructor).addDependency(_void);
	}

	@Test
	public void implementsGenericInterface() throws IOException {
		ClassDescriptor extendsGenericClass = stubClass(ImplementsGenericInterface.class);
		ClassDescriptor javaUtilIterable = stubClass(Iterable.class);
		ClassDescriptor javaLangNumber = stubClass(Number.class);
		MethodDescriptor constructor = mock(MethodDescriptor.class);
		when(
				store.resolveMethodDescriptor(extendsGenericClass,
						"void <init>()")).thenReturn(constructor);

		scanner.scanClass(ImplementsGenericInterface.class);

		verify(extendsGenericClass).addImplements(javaUtilIterable);
		verify(extendsGenericClass).addDependency(javaLangNumber);
		verify(extendsGenericClass).addChild(constructor);
		verify(constructor).addDependency(_void);

	}
}
