package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ServiceLoaderDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;

/**
 * Implementation of the
 * {@link com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin}
 * for java packages.
 */
@Requires(FileDescriptor.class)
public class ServiceLoaderFileScannerPlugin extends AbstractScannerPlugin<FileResource, ServiceLoaderDescriptor> {

    private static final Pattern PATTERN = Pattern.compile("(.*/)?META-INF/services/(.*)");

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope) && PATTERN.matcher(path).matches();
    }

    @Override
    public ServiceLoaderDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Matcher matcher = PATTERN.matcher(path);
        if (!matcher.matches()) {
            throw new IOException("Cannot match path name: " + path);
        }
        String serviceInterface = matcher.group(2);
        ScannerContext context = scanner.getContext();
        FileDescriptor fileDescriptor = context.peek(FileDescriptor.class);
        ServiceLoaderDescriptor serviceLoaderDescriptor = context.getStore().addDescriptorType(fileDescriptor, ServiceLoaderDescriptor.class);
        TypeDescriptor interfaceTypeDescriptor = getTypeDescriptor(serviceInterface, context);
        serviceLoaderDescriptor.setType(interfaceTypeDescriptor);

        try (InputStream stream = item.createStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String serviceImplementation;
            while ((serviceImplementation = reader.readLine()) != null) {
                if (!isEmptyOrComment(serviceImplementation)) {
                    TypeDescriptor implementationTypeDescriptor = getTypeDescriptor(serviceImplementation, context);
                    serviceLoaderDescriptor.getContains().add(implementationTypeDescriptor);
                }
            }
        }
        return serviceLoaderDescriptor;
    }

    private boolean isEmptyOrComment(String line) {
        return StringUtils.isEmpty(line) || StringUtils.trimToEmpty(line).startsWith("#");
    }

    private TypeDescriptor getTypeDescriptor(String fqn, ScannerContext scannerContext) {
        TypeResolver typeResolver = scannerContext.peek(TypeResolver.class);
        return typeResolver.resolve(fqn, scannerContext).getTypeDescriptor();
    }

}
