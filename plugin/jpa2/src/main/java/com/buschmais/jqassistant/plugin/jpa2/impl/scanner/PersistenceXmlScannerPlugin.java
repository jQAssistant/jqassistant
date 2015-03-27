package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import static com.sun.java.xml.ns.persistence.Persistence.PersistenceUnit;
import static com.sun.java.xml.ns.persistence.Persistence.PersistenceUnit.Properties.Property;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceUnitDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceXmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.JAXBUnmarshaller;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;
import com.sun.java.xml.ns.persistence.Persistence;
import com.sun.java.xml.ns.persistence.PersistenceUnitCachingType;
import com.sun.java.xml.ns.persistence.PersistenceUnitTransactionType;
import com.sun.java.xml.ns.persistence.PersistenceUnitValidationModeType;

/**
 * A scanner for JPA model units.
 */
public class PersistenceXmlScannerPlugin extends AbstractScannerPlugin<FileResource, PersistenceXmlDescriptor> {


    private JAXBUnmarshaller<FileResource, Persistence> unmarshaller;

    @Override
    public void initialize() {
        unmarshaller = new JAXBUnmarshaller<>(Persistence.class);
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return JavaScope.CLASSPATH.equals(scope) && "/META-INF/persistence.xml".equals(path) || "/WEB-INF/persistence.xml".equals(path);
    }

    @Override
    public PersistenceXmlDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Persistence persistence = unmarshaller.unmarshal(item);
        Store store = scanner.getContext().getStore();
        XmlFileDescriptor xmlFileDescriptor = scanner.scan(item, path, XmlScope.DOCUMENT);
        PersistenceXmlDescriptor persistenceXmlDescriptor = store.addDescriptorType(xmlFileDescriptor, PersistenceXmlDescriptor.class);
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
