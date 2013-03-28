package com.buschmais.jqassistant.store.api;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public interface Store {

	void reset();

	void start();

	void stop();

	void beginTransaction();

	void endTransaction();

	PackageDescriptor resolvePackageDescriptor(String fullQualifiedName);

	ClassDescriptor resolveClassDescriptor(String fullQualifiedName);

	MethodDescriptor resolveMethodDescriptor(String fullQualifiedName);

	FieldDescriptor resolveFieldDescriptor(String fullQualifiedName);

}