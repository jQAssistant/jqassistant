package com.buschmais.jqassistant.model;

public abstract class AbstractDescriptor {

    private String name;

    /**
     * @param name
     */
    public AbstractDescriptor(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getFullQualifiedName();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractDescriptor other = (AbstractDescriptor) obj;
        String fullQualifiedName = getFullQualifiedName();
        String otherFullQualifiedName = other.getFullQualifiedName();
        if (fullQualifiedName == null) {
            if (otherFullQualifiedName != null)
                return false;
        } else if (!fullQualifiedName.equals(otherFullQualifiedName))
            return false;
        return true;
    }

}
