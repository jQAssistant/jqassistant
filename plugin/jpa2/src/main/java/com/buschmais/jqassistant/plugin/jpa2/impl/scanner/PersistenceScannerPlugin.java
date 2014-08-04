package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import static com.sun.java.xml.ns.persistence.Persistence.PersistenceUnit;
import static com.sun.java.xml.ns.persistence.Persistence.PersistenceUnit.Properties.Property;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceUnitDescriptor;
import com.sun.java.xml.ns.persistence.ObjectFactory;
import com.sun.java.xml.ns.persistence.Persistence;
import com.sun.java.xml.ns.persistence.PersistenceUnitCachingType;
import com.sun.java.xml.ns.persistence.PersistenceUnitTransactionType;
import com.sun.java.xml.ns.persistence.PersistenceUnitValidationModeType;

/**
 * A scanner for JPA model units.
 */
public class PersistenceScannerPlugin extends AbstractScannerPlugin<FileResource> {

    private JAXBContext jaxbContext;

    private DescriptorResolverFactory descriptorResolverFactory;

    @Override
    protected void initialize() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context.", e);
        }
        descriptorResolverFactory = new DescriptorResolverFactory(getStore());
    }

    @Override
    public Class<? super FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return JavaScope.CLASSPATH.equals(scope) && "/META-INF/persistence.xml".equals(path) || "/WEB-INF/persistence.xml".equals(path);
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Persistence persistence;
        try (InputStream stream = item.createStream()) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            persistence = unmarshaller.unmarshal(new StreamSource(stream), Persistence.class).getValue();
        } catch (JAXBException e) {
            throw new IOException("Cannot read model descriptor.", e);
        }
        Store store = getStore();
        PersistenceDescriptor persistenceDescriptor = store.create(PersistenceDescriptor.class);
        persistenceDescriptor.setVersion(persistence.getVersion());
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
                TypeDescriptor typeDescriptor = descriptorResolverFactory.getTypeDescriptorResolver().resolve(clazz);
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
            persistenceDescriptor.getContains().add(persistenceUnitDescriptor);
        }
        persistenceDescriptor.setFileName(path);
        return asList(persistenceDescriptor);
    }
}
