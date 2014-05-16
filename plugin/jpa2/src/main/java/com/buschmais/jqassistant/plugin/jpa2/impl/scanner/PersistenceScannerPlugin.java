package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import static com.sun.java.xml.ns.persistence.Persistence.PersistenceUnit;
import static com.sun.java.xml.ns.persistence.Persistence.PersistenceUnit.Properties.Property;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor.PersistenceDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor.PersistenceUnitDescriptor;
import com.sun.java.xml.ns.persistence.ObjectFactory;
import com.sun.java.xml.ns.persistence.Persistence;
import com.sun.java.xml.ns.persistence.PersistenceUnitCachingType;
import com.sun.java.xml.ns.persistence.PersistenceUnitTransactionType;
import com.sun.java.xml.ns.persistence.PersistenceUnitValidationModeType;

/**
 * A scanner for JPA model units.
 */
public class PersistenceScannerPlugin extends AbstractFileScannerPlugin {

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
    public boolean matches(String file, boolean isDirectory) {
        return "META-INF/persistence.xml".equals(file) || "WEB-INF/persistence.xml".equals(file);
    }

    @Override
    public PersistenceDescriptor scanFile(StreamSource streamSource) throws IOException {
        Persistence persistence;
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            persistence = unmarshaller.unmarshal(streamSource, Persistence.class).getValue();
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
        return persistenceDescriptor;
    }

    @Override
    public PersistenceDescriptor scanDirectory(String name) throws IOException {
        return null;
    }
}
