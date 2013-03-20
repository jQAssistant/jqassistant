package com.buschmais.jqassistant.store.model;

public class ClassDescriptor extends AbstractDescriptor implements Comparable<ClassDescriptor> {

    private PackageDescriptor packageDescriptor;

    public ClassDescriptor(PackageDescriptor packageDescriptor, String name) {
        super(name);
        this.packageDescriptor = packageDescriptor;
    }

    public PackageDescriptor getPackageDescriptor() {
        return packageDescriptor;
    }

    @Override
    public String getFullQualifiedName() {
        StringBuffer buffer = new StringBuffer();
        if (packageDescriptor != null) {
            buffer.append(packageDescriptor.getFullQualifiedName());
            buffer.append('.');
        }
        buffer.append(getName());
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((packageDescriptor == null) ? 0 : packageDescriptor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassDescriptor other = (ClassDescriptor) obj;
        if (packageDescriptor == null) {
            if (other.packageDescriptor != null)
                return false;
        } else if (!packageDescriptor.equals(other.packageDescriptor))
            return false;
        return true;
    }

    @Override
    public int compareTo(ClassDescriptor o) {
        int result = this.getPackageDescriptor().compareTo(o.getPackageDescriptor());
        if (result == 0) {
            result = this.getName().compareTo(o.getName());
        }
        return result;
    }

}
