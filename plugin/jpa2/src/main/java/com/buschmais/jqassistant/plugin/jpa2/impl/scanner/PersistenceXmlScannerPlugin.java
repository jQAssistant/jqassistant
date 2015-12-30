package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import static org.jcp.xmlns.xml.ns.persistence.Persistence.PersistenceUnit;
import static org.jcp.xmlns.xml.ns.persistence.Persistence.PersistenceUnit.Properties.Property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jcp.xmlns.xml.ns.persistence.Persistence;
import org.jcp.xmlns.xml.ns.persistence.PersistenceUnitCachingType;
import org.jcp.xmlns.xml.ns.persistence.PersistenceUnitTransactionType;
import org.jcp.xmlns.xml.ns.persistence.PersistenceUnitValidationModeType;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceUnitDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceXmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;
import com.buschmais.jqassistant.plugin.xml.api.scanner.JAXBUnmarshaller;

/**
 * A scanner for JPA model units.
 */
@Requires(FileDescriptor.class)
public class PersistenceXmlScannerPlugin extends AbstractXmlFileScannerPlugin<PersistenceXmlDescriptor, PersistenceXmlScannerPlugin> {

    private JAXBUnmarshaller<Persistence> unmarshaller;

    @Override
    protected PersistenceXmlScannerPlugin getThis() {
        return this;
    }

    @Override
    public void initialize() {
        Map<String, String> namespaceMapping = new HashMap<>();
        namespaceMapping.put("http://java.sun.com/xml/ns/persistence", "http://xmlns.jcp.org/xml/ns/persistence");
        unmarshaller = new JAXBUnmarshaller<>(Persistence.class, namespaceMapping);
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return JavaScope.CLASSPATH.equals(scope) && "/META-INF/persistence.xml".equals(path) || "/WEB-INF/persistence.xml".equals(path);
    }

    @Override
    public PersistenceXmlDescriptor scan(FileResource item, PersistenceXmlDescriptor persistenceXmlDescriptor, String path, Scope scope, Scanner scanner) throws IOException {
        Store store = scanner.getContext().getStore();
        Persistence persistence = unmarshaller.unmarshal(item);
        persistenceXmlDescriptor.setVersion(persistence.getVersion());
        // Create model units
        for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
            PersistenceUnitDescriptor persistenceUnitDescriptor = store.create(PersistenceUnitDescriptor.class);
            persistenceUnitDescriptor.setName(persistenceUnit.getName());
            PersistenceUnitTransactionType transactionType = persistenceUnit.getTransactionType();
            if (transactionType != null) {
                persistenceUnitDescriptor.setTransactionType(transactionType.name());
            }
            persistenceUnitDescriptor.setDescription(persistenceUnit.getDescription());
            persistenceUnitDescriptor.setJtaDataSource(persistenceUnit.getJtaDataSource());
            persistenceUnitDescriptor.setNonJtaDataSource(persistenceUnit.getNonJtaDataSource());
            persistenceUnitDescriptor.setProvider(persistenceUnit.getProvider());
            PersistenceUnitValidationModeType validationMode = persistenceUnit.getValidationMode();
            if (validationMode != null) {
                persistenceUnitDescriptor.setValidationMode(validationMode.name());
            }
            PersistenceUnitCachingType sharedCacheMode = persistenceUnit.getSharedCacheMode();
            if (sharedCacheMode != null) {
                persistenceUnitDescriptor.setSharedCacheMode(sharedCacheMode.name());
            }
            for (String clazz : persistenceUnit.getClazz()) {
                TypeDescriptor typeDescriptor = scanner.getContext().peek(TypeResolver.class).resolve(clazz, scanner.getContext()).getTypeDescriptor();
                persistenceUnitDescriptor.getContains().add(typeDescriptor);
            }
            // Create persistence unit properties
            PersistenceUnit.Properties properties = persistenceUnit.getProperties();
            if (properties != null) {
                for (Property property : properties.getProperty()) {
                    PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
                    propertyDescriptor.setName(property.getName());
                    propertyDescriptor.setValue(property.getValue());
                    persistenceUnitDescriptor.getProperties().add(propertyDescriptor);
                }
            }
            // Add model unit to model descriptor
            persistenceXmlDescriptor.getContains().add(persistenceUnitDescriptor);
        }
        return persistenceXmlDescriptor;
    }
}
