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

	PackageDescriptor resolvePackageDescriptor(
			PackageDescriptor packageDescriptor, String packageName);

	ClassDescriptor resolveClassDescriptor(PackageDescriptor packageDescriptor,
			String className);

	MethodDescriptor resolveMethodDescriptor(ClassDescriptor classDescriptor,
			String methodName);

	FieldDescriptor resolveFieldDescriptor(ClassDescriptor classDescriptor,
			String fieldName);

}