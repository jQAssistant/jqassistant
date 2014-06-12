package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ServiceLoaderDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * Implementation of the
 * {@link com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin}
 * for java packages.
 */
public class ServiceLoaderFileScannerPlugin extends AbstractScannerPlugin<InputStream> {

    private static final Pattern PATTERN = Pattern.compile("(.*/)?META-INF/services/(.*)");

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public boolean accepts(InputStream item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope) && PATTERN.matcher(path).matches();
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(InputStream item, String path, Scope scope, Scanner scanner) throws IOException {
        Matcher matcher = PATTERN.matcher(path);
        if (matcher.matches()) {
            String serviceInterface = matcher.group(2);
            ServiceLoaderDescriptor serviceLoaderDescriptor = getStore().create(ServiceLoaderDescriptor.class);
            TypeDescriptor interfaceTypeDescriptor = getTypeDescriptor(serviceInterface);
            serviceLoaderDescriptor.setType(interfaceTypeDescriptor);
            BufferedReader reader = new BufferedReader(new InputStreamReader(item));
            String serviceImplementation;
            while ((serviceImplementation = reader.readLine()) != null) {
                TypeDescriptor implementationTypeDescriptor = getTypeDescriptor(serviceImplementation);
                serviceLoaderDescriptor.getContains().add(implementationTypeDescriptor);
            }
            serviceLoaderDescriptor.setFileName(path);
            return asList(serviceLoaderDescriptor);
        }
        return emptyList();
    }

    private TypeDescriptor getTypeDescriptor(String fqn) {
        TypeDescriptor typeDescriptor = getStore().find(TypeDescriptor.class, fqn);
        if (typeDescriptor == null) {
            typeDescriptor = getStore().create(TypeDescriptor.class, fqn);
        }
        return typeDescriptor;
    }

}
