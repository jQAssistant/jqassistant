package com.buschmais.jqassistant.store.api.model;

public interface PackageDescriptor extends ParentDescriptor {

	void addClass(ClassDescriptor classDescriptor);

	void addPackage(PackageDescriptor packageDescriptor);
}
