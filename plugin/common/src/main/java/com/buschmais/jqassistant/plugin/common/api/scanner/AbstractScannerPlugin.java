package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Abstract base implementation of a {@link ScannerPlugin}.
 */
public abstract class AbstractScannerPlugin<I, D extends Descriptor> implements ScannerPlugin<I, D> {

    private Map<String, Object> properties;

    @Override
    public void initialize(Map<String, Object> properties) {
        this.properties = properties;
        initialize();
    }

    @Override
    public Class<? extends I> getType() {
        return getType(AbstractScannerPlugin.class, 0);
    }

    @Override
    public Class<? extends D> getDescriptorType() {
        return getType(AbstractScannerPlugin.class, 1);
    }

    /**
     * Determines the type parameter for a generic super class.
     * 
     * @param expectedSuperClass
     *            The generic super class.
     * @param genericTypeParameterIndex
     *            The index, e.g. 0 for the first.
     * @return The type parameter.
     */
    protected <T> Class<T> getType(Class<?> expectedSuperClass, int genericTypeParameterIndex) {
        Class<? extends AbstractScannerPlugin> thisClass = this.getClass();
        if (!thisClass.getSuperclass().equals(expectedSuperClass)) {
            throw new IllegalStateException("Cannot determine type argument of " + thisClass.getName());
        }
        Type genericSuperclass = thisClass.getGenericSuperclass();
        Type typeParameter = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[genericTypeParameterIndex];
        if (typeParameter instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) typeParameter).getRawType();
        }
        return (Class<T>) typeParameter;
    }

    /**
     * Initialize the plugin.
     */
    protected void initialize() {
    }

    /**
     * Get all properties.
     * 
     * @return The properties.
     */
    protected Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Get a property as string.
     * 
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return The value.
     * @throws IOException
     */
    protected String getStringProperty(String name, String defaultValue) {
        return getProperty(name, String.class, defaultValue);
    }

    /**
     * Get a property using a specified type.
     * 
     * @param name
     *            The name.
     * @param type
     *            The type.
     * @param defaultValue
     *            The default value.
     * @param <T>
     *            The type.
     * @return The value.
     */
    private <T> T getProperty(String name, Class<T> type, T defaultValue) {
        Object o = properties.get(name);
        if (o == null) {
            return defaultValue;
        }
        if (!type.isAssignableFrom(o.getClass())) {
            throw new IllegalArgumentException("Found value of type " + o.getClass().getName() + "for property " + name + ", expected " + type.getName());
        }
        return type.cast(o);
    }

    /**
     * Return the relative path of a file within a directory.
     * 
     * @param directory
     *            The directory.
     * @param entry
     *            The file.
     * @return The relative path.
     */
    protected String getDirectoryPath(File directory, File entry) {
        String relativePath;
        if (entry.equals(directory)) {
            relativePath = "/";
        } else {
            String filePath = entry.getAbsolutePath();
            String directoryPath = directory.getAbsolutePath();
            relativePath = filePath.substring(directoryPath.length()).replace(File.separator, "/");
        }
        return relativePath;
    }
}
