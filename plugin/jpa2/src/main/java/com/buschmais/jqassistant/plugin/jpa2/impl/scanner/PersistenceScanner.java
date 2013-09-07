package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.PersistenceDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.PersistenceUnitDescriptor;
import com.sun.java.xml.ns.persistence.ObjectFactory;
import com.sun.java.xml.ns.persistence.Persistence;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

import static com.sun.java.xml.ns.persistence.Persistence.PersistenceUnit;

/**
 * A scanner for JPA persistence units.
 */
public class PersistenceScanner implements FileScannerPlugin<PersistenceDescriptor> {

    private JAXBContext jaxbContext;

    public PersistenceScanner() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return "META-INF/persistence.xml".equals(file) || "WEB-INF/persistence.xml".equals(file);
    }

    @Override
    public PersistenceDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        DescriptorResolverFactory descriptorResolverFactory = new DescriptorResolverFactory(store);
        Persistence persistence;
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            persistence = unmarshaller.unmarshal(streamSource, Persistence.class).getValue();
        } catch (JAXBException e) {
            throw new IOException("Cannot read persistence descriptor.", e);
        }
        PersistenceDescriptor persistenceDescriptor = store.create(PersistenceDescriptor.class, streamSource.getSystemId());
        for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
            PersistenceUnitDescriptor persistenceUnitDescriptor = store.create(PersistenceUnitDescriptor.class, persistenceUnit.getName());
            persistenceDescriptor.getContains().add(persistenceUnitDescriptor);
            for (String clazz : persistenceUnit.getClazz()) {
                TypeDescriptor typeDescriptor = descriptorResolverFactory.getTypeDescriptorResolver().resolve(clazz);
                persistenceUnitDescriptor.getContains().add(typeDescriptor);
            }
        }
        return persistenceDescriptor;
    }

    @Override
    public PersistenceDescriptor scanDirectory(Store store, String name) throws IOException {
        return null;
    }
}
