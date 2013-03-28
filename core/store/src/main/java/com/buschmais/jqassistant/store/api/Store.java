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

	PackageDescriptor getPackageDescriptor(String fullQualifiedName);

	ClassDescriptor getClassDescriptor(String fullQualifiedName);

	ClassDescriptor createClassDescriptor(String fullQualifiedName);

	MethodDescriptor getMethodDescriptor(String fullQualifiedName);

	MethodDescriptor createMethodDescriptor(String fullQualifiedName);

	FieldDescriptor getFieldDescriptor(String fullQualifiedName);

	FieldDescriptor createFieldDescriptor(String fullQualifiedName);

}