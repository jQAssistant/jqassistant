package com.buschmais.jqassistant.store.api.model;

public interface MethodDescriptor extends DependentDescriptor {

	void addThrows(ClassDescriptor exception);
}
