package com.buschmais.jqassistant.store.model;

public class MethodDescriptor extends AbstractDescriptor implements Comparable<MethodDescriptor> {

    private ClassDescriptor classDescriptor;

    public MethodDescriptor(ClassDescriptor classDescriptor, String name) {
        super(name);
        this.classDescriptor = classDescriptor;
    }

    public ClassDescriptor getPackageDescriptor() {
        return classDescriptor;
    }

    @Override
    public String getFullQualifiedName() {
        StringBuffer buffer = new StringBuffer();
        if (classDescriptor != null) {
            buffer.append(classDescriptor.getFullQualifiedName());
            buffer.append('#');
        }
        buffer.append(getName());
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((classDescriptor == null) ? 0 : classDescriptor.hashCode());
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
        MethodDescriptor other = (MethodDescriptor) obj;
        if (classDescriptor == null) {
            if (other.classDescriptor != null)
                return false;
        } else if (!classDescriptor.equals(other.classDescriptor))
            return false;
        return true;
    }

    @Override
    public int compareTo(MethodDescriptor o) {
        int result = this.getPackageDescriptor().compareTo(o.getPackageDescriptor());
        if (result == 0) {
            result = this.getName().compareTo(o.getName());
        }
        return result;
    }

}
