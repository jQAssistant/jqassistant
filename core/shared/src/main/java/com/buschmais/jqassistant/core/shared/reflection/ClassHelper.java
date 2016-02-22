package com.buschmais.jqassistant.core.shared.reflection;

/**
 * Provides functionality related to class loading and instance creation.
 */
public final class ClassHelper {

    private ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param classLoader The classloader to use.
     */
    public ClassHelper(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Create and return an instance of the given type name.
     *
     * @param typeName The type name.
     * @param <T>      The type.
     * @return The instance.
     */
    public <T> Class<T> getType(String typeName) {
        try {
            return (Class<T>) classLoader.loadClass(typeName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find class " + typeName, e);
        }
    }

    /**
     * Create an instance of the given scanner plugin class.
     *
     * @param type     The type.
     * @param typeName The type name.
     * @param <T>      The type.
     * @return The scanner plugin instance.
     */
    public <T> T createInstance(Class<T> type, String typeName) {
        try {
            return type.cast(getType(typeName).newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot create instance of class " + type.getName(), e);
        }
    }
}
