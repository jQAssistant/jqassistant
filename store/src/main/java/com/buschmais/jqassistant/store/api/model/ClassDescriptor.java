package com.buschmais.jqassistant.store.api.model;

public interface ClassDescriptor extends Descriptor {

	void addDependency(ClassDescriptor dependency);

	void addSuperClass(ClassDescriptor superClass);

	void addImplements(ClassDescriptor interfaceClass);
}
