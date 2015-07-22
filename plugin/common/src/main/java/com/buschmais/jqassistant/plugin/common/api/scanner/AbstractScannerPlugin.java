package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Abstract base implementation of a {@link ScannerPlugin}.
 */
public abstract class AbstractScannerPlugin<I, D extends Descriptor> implements ScannerPlugin<I, D> {

    private Map<String, Object> properties;

    private ScannerContext scannerContext;

    /**
     * Initialize the plugin.
     */
    public void initialize() {
    }

    @Override
    public final void configure(ScannerContext scannerContext, Map<String, Object> properties) {
        this.scannerContext = scannerContext;
        this.properties = properties;
        configure();
    }

    /**
     * Convenience method which might be overridden by sub-classes.
     */
    protected void configure() {
    }

    @Override
    public Class<? extends I> getType() {
        return getTypeParameter(AbstractScannerPlugin.class, 0);
    }

    @Override
    public Class<? extends D> getDescriptorType() {
        return getTypeParameter(AbstractScannerPlugin.class, 1);
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
    protected <T> Class<T> getTypeParameter(Class<?> expectedSuperClass, int genericTypeParameterIndex) {
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
     * Get all properties.
     * 
     * @return The properties.
     */
    protected Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Get the value of a property.
     * 
     * @param name
     *            The name of the property.
     * @param type
     *            The expected type.
     * @param <T>
     *            The type.
     * @return The value.
     */
    protected <T> T getProperty(String name, Class<T> type) {
        return type.cast(properties.get(name));
    }

    /**
     * Get a property as string.
     * 
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    protected String getStringProperty(String name, String defaultValue) {
        Object value = properties.get(name);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Get a property as boolean.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    protected Boolean getBooleanProperty(String name, Boolean defaultValue) {
        Object value = properties.get(name);
        return value != null ? Boolean.valueOf(value.toString()) : defaultValue;
    }

    /**
     * Return the scanner context.
     * 
     * @return The scanner context.
     */
    protected ScannerContext getScannerContext() {
        return scannerContext;
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

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }
}
