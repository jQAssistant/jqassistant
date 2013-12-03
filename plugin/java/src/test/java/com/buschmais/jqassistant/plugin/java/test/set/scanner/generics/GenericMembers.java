package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.util.List;

public class GenericMembers {

	private List<Integer> integerList;

	<X extends Number> X get(X value) {
		return null;
	}

}
