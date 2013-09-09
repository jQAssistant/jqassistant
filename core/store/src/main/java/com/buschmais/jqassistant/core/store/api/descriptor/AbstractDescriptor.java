package com.buschmais.jqassistant.core.store.api.descriptor;

/**
 * Abstract implementation of a descriptor.
 */
public abstract class AbstractDescriptor implements Descriptor {

    /**
     * The unique id of this descriptor.
     */
    private Long id;

    /**
     * The full qualified name.
     */
    private String fullQualifiedName;

    /**
     * Return the unique id.
     *
     * @return The unique id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the unique id.
     *
     * @param id The unique id.
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

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
    public final String toString() {
        return this.getClass().getSimpleName() + " [id=" + id + ", fullQualifiedName=" + fullQualifiedName + "]";
    }
}
