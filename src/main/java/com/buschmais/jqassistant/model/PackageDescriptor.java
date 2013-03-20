package com.buschmais.jqassistant.model;

public class PackageDescriptor extends AbstractDescriptor implements Comparable<PackageDescriptor> {

    private PackageDescriptor parent;

    public PackageDescriptor(PackageDescriptor parent, String name) {
        super(name);
        this.parent = parent;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public int compareTo(PackageDescriptor o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public String getFullQualifiedName() {
        StringBuffer buffer = new StringBuffer();
        if (parent != null) {
            buffer.append(parent.getFullQualifiedName());
            buffer.append(".");
        }
        buffer.append(getName());
        return buffer.toString();
    }

    public PackageDescriptor getParent() {
        return parent;
    }

}
