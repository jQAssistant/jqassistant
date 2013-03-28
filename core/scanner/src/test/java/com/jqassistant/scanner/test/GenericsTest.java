package com.jqassistant.scanner.test;

import java.io.IOException;

import org.junit.Test;

import com.jqassistant.scanner.test.sets.generics.BoundGenericType;
import com.jqassistant.scanner.test.sets.generics.ExtendsGenericClass;
import com.jqassistant.scanner.test.sets.generics.GenericType;
import com.jqassistant.scanner.test.sets.generics.ImplementsGenericInterface;
import com.jqassistant.scanner.test.sets.generics.NestedGenericMethod;
import com.jqassistant.scanner.test.sets.generics.NestedGenericType;

public class GenericsTest extends AbstractScannerTest {

	@Test
	public void genericType() throws IOException {
		scanner.scanClass(GenericType.class);
	}

	@Test
	public void boundGenericType() throws IOException {
		scanner.scanClass(BoundGenericType.class);
	}

	@Test
	public void nestedGenericType() throws IOException {
		scanner.scanClass(NestedGenericType.class);
	}

	@Test
	public void nestedGenericMethod() throws IOException {
		scanner.scanClass(NestedGenericMethod.class);
	}

	@Test
	public void extendsGenericClass() throws IOException {
		scanner.scanClass(ExtendsGenericClass.class);
	}

	@Test
	public void implementsGenericInterface() throws IOException {
		scanner.scanClass(ImplementsGenericInterface.class);
	}
}
