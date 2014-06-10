package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyFileDescriptor;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin}
 * for property files.
 */
public class PropertyFileScannerPlugin extends AbstractScannerPlugin<InputStream> {

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public boolean accepts(InputStream item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope) && path.endsWith(".properties");
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(InputStream item, String path, Scope scope, Scanner scanner) throws IOException {
        Store store = getStore();
        PropertyFileDescriptor propertyFileDescriptor = store.create(PropertyFileDescriptor.class);
        Properties properties = new Properties();
        properties.load(item);
        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);
            PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
            propertyDescriptor.setName(name);
            propertyDescriptor.setValue(value);
            propertyFileDescriptor.getProperties().add(propertyDescriptor);
        }
        return asList(propertyFileDescriptor);
    }

}
