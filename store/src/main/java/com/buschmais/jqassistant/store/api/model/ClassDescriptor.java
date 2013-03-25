package com.buschmais.jqassistant.store.api.model;

public interface ClassDescriptor extends ParentDescriptor, DependentDescriptor {

    PackageDescriptor getPackageDescriptor();

    void addSuperClass(ClassDescriptor superClass);

    void addImplements(ClassDescriptor interfaceClass);

}
