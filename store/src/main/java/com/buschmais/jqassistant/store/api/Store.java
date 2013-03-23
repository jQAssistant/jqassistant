package com.buschmais.jqassistant.store.api;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public interface Store {

	void start();

	void stop();

	void beginTransaction();

	void endTransaction();

	ClassDescriptor getClassDescriptor(String fullQualifiedName);

	ClassDescriptor createClassDescriptor(String fullQualifiedName);

	PackageDescriptor getPackageDescriptor(String fullQualifiedName);

}