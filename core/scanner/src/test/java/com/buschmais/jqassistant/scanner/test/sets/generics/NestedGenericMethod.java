package com.buschmais.jqassistant.scanner.test.sets.generics;

public class NestedGenericMethod {

	<X, Y extends GenericType<X>> X get(Y value) {
		return null;
	}

}
