package com.buschmais.jqassistant.core.store.api.descriptor;

/**
 * Abstract implementation of a descriptor.
 */
public abstract class AbstractFullQualifiedNameDescriptor extends AbstractDescriptor implements FullQualifiedNameDescriptor {

    /**
     * The full qualified name.
     */
    private String fullQualifiedName;

    /**
     * Return the full qualified name.
     *
     * @return The full qualified name.
     */
    @Override
    public String getFullQualifiedName() {
        return fullQualifiedName;
    }

    /**
     * Set the full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     */
    @Override
    public void setFullQualifiedName(String fullQualifiedName) {
        this.fullQualifiedName = fullQualifiedName;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [id=" + getId() + ", fullQualifiedName=" + fullQualifiedName + "]";
    }
}
