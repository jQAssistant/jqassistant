package com.buschmais.jqassistant.plugin.java.test.set.dependency.types;

/**
 * A types with dependencies on types level.
 */
@TypeAnnotation
public class DependentType extends SuperType implements Comparable<Integer> {

	@Override
	public int compareTo(Integer o) {
		return 0;
	}
}
