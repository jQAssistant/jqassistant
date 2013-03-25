package com.jqassistant.scanner.test;

import java.io.IOException;

import org.junit.Test;

import com.jqassistant.scanner.test.sets.generics.BoundGenericType;
import com.jqassistant.scanner.test.sets.generics.GenericType;
import com.jqassistant.scanner.test.sets.generics.NestedGenericMethod;
import com.jqassistant.scanner.test.sets.generics.NestedGenericType;

public class GenericsTest extends AbstractScannerTest {

	@Test
	public void genericType() throws IOException {
		scanClass(GenericType.class);
	}

	@Test
	public void boundGenericType() throws IOException {
		scanClass(BoundGenericType.class);
	}

	@Test
	public void nestedGenericType() throws IOException {
		scanClass(NestedGenericType.class);
	}

	@Test
	public void nestedGenericMethod() throws IOException {
		scanClass(NestedGenericMethod.class);
	}
}
