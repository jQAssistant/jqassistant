package com.buschmais.jqassistant.core.model.api.descriptor;

/**
 * Describes a Java package.
 */
public class PackageDescriptor extends ParentDescriptor implements NamedDescriptor {

    /**
     * The name of the package.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
