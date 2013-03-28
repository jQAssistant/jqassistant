package com.buschmais.jqassistant.store.api.model;

public interface DependentDescriptor extends Descriptor {

	void addDependency(ClassDescriptor dependency);

}
