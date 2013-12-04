package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.util.List;
import java.util.Set;

public class GenericMembers {

	private List<Integer> integerList;

	<X extends Number> Set<X> get(List<Double> value) {
		return null;
	}

}
