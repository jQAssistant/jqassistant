package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ServiceLoaderDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * Implementation of the
 * {@link com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractFileScannerPlugin}
 * for java packages.
 */
public class ServiceLoaderScannerPlugin extends AbstractFileScannerPlugin {

    private static final Pattern PATTERN = Pattern.compile("(.*/)?META-INF/services/(.*)");

    @Override
    protected void initialize() {
    }

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && PATTERN.matcher(file).matches();
    }

    @Override
    public ServiceLoaderDescriptor scanFile(StreamSource streamSource) throws IOException {
        String systemId = streamSource.getSystemId();
        Matcher matcher = PATTERN.matcher(systemId);
        if (!matcher.matches()) {
            return null;
        }
        String serviceInterface = matcher.group(2);
        ServiceLoaderDescriptor serviceLoaderDescriptor = getStore().create(ServiceLoaderDescriptor.class);
        TypeDescriptor interfaceTypeDescriptor = getTypeDescriptor(serviceInterface);
        serviceLoaderDescriptor.setType(interfaceTypeDescriptor);
        BufferedReader reader = new BufferedReader(new InputStreamReader(streamSource.getInputStream()));
        String serviceImplementation;
        while ((serviceImplementation = reader.readLine()) != null) {
            TypeDescriptor implementationTypeDescriptor = getTypeDescriptor(serviceImplementation);
            serviceLoaderDescriptor.getContains().add(implementationTypeDescriptor);
        }
        return serviceLoaderDescriptor;
    }

    @Override
    public FileDescriptor scanDirectory(String name) throws IOException {
        return null;
    }

    private TypeDescriptor getTypeDescriptor(String fqn) {
        TypeDescriptor typeDescriptor = getStore().find(TypeDescriptor.class, fqn);
        if (typeDescriptor == null) {
            typeDescriptor = getStore().create(TypeDescriptor.class, fqn);
        }
        return typeDescriptor;
    }
}
