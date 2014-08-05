package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.ServiceLoaderDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Implementation of the
 * {@link com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin}
 * for java packages.
 */
public class ServiceLoaderFileScannerPlugin extends AbstractScannerPlugin<FileResource> {

    private static final Pattern PATTERN = Pattern.compile("(.*/)?META-INF/services/(.*)");

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope) && PATTERN.matcher(path).matches();
    }

    @Override
    public FileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Matcher matcher = PATTERN.matcher(path);
        if (!matcher.matches()) {
            throw new IOException("Cannot match path name: " + path);
        }
        String serviceInterface = matcher.group(2);
        ServiceLoaderDescriptor serviceLoaderDescriptor = getStore().create(ServiceLoaderDescriptor.class);
        TypeDescriptor interfaceTypeDescriptor = getTypeDescriptor(serviceInterface);
        serviceLoaderDescriptor.setType(interfaceTypeDescriptor);
        try (InputStream stream = item.createStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String serviceImplementation;
            while ((serviceImplementation = reader.readLine()) != null) {
                TypeDescriptor implementationTypeDescriptor = getTypeDescriptor(serviceImplementation);
                serviceLoaderDescriptor.getContains().add(implementationTypeDescriptor);
            }
            serviceLoaderDescriptor.setFileName(path);
        }
        return serviceLoaderDescriptor;
    }

    private TypeDescriptor getTypeDescriptor(String fqn) {
        TypeDescriptor typeDescriptor = getStore().find(TypeDescriptor.class, fqn);
        if (typeDescriptor == null) {
            typeDescriptor = getStore().create(TypeDescriptor.class, fqn);
        }
        return typeDescriptor;
    }

}
