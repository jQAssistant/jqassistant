package com.buschmais.jqassistant.model;

public class PackageDescriptor extends AbstractDescriptor implements Comparable<PackageDescriptor> {

    public PackageDescriptor(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public int compareTo(PackageDescriptor o) {
        return this.getName().compareTo(o.getName());
    }

}
