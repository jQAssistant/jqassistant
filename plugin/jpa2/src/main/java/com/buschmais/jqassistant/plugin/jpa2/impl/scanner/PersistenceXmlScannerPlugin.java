package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

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

/**
 * A scanner for JPA model units.
 */
@Requires(FileDescriptor.class)
public class PersistenceXmlScannerPlugin extends AbstractXmlFileScannerPlugin<PersistenceXmlDescriptor> {

    private PersistanceXMLUnmarshaller unmarshaller = new PersistanceXMLUnmarshaller();

    @Override
    public void initialize() {
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return JavaScope.CLASSPATH.equals(scope) && "/META-INF/persistence.xml".equals(path) || "/WEB-INF/persistence.xml".equals(path);
    }

    @Override
    public PersistenceXmlDescriptor scan(FileResource item, PersistenceXmlDescriptor persistenceXmlDescriptor, String path, Scope scope, Scanner scanner) throws IOException {
        Store store = scanner.getContext().getStore();
        PersistenceView persistenceView = unmarshaller.unmarshal(item);
        persistenceXmlDescriptor.setVersion(persistenceView.getVersion());

        // Create model units

        for (PersistenceUnitView persistenceUnit : persistenceView.getPersistenceUnits()) {
            PersistenceUnitDescriptor persistenceUnitDescriptor = store.create(PersistenceUnitDescriptor.class);
            persistenceUnitDescriptor.setName(persistenceUnit.getName());
            String transactionType = persistenceUnit.getTransactionType();
            if (transactionType != null) {
                persistenceUnitDescriptor.setTransactionType(transactionType);
            }
            persistenceUnitDescriptor.setDescription(persistenceUnit.getDescription());
            persistenceUnitDescriptor.setJtaDataSource(persistenceUnit.getJtaDataSource());
            persistenceUnitDescriptor.setNonJtaDataSource(persistenceUnit.getNonJtaDataSource());
            persistenceUnitDescriptor.setProvider(persistenceUnit.getProvider());
            persistenceUnitDescriptor.setExcludingUnlistedClasses(persistenceUnit.isExcludingUnlistedClasses());
            String validationMode = persistenceUnit.getValidationMode();

            if (validationMode != null) {
                persistenceUnitDescriptor.setValidationMode(validationMode);
            }
            String sharedCacheMode = persistenceUnit.getSharedCacheMode();
            if (sharedCacheMode != null) {
                persistenceUnitDescriptor.setSharedCacheMode(sharedCacheMode);
            }
            for (String clazz : persistenceUnit.getClazz()) {
                TypeDescriptor typeDescriptor = scanner.getContext().peek(TypeResolver.class).resolve(clazz, scanner.getContext()).getTypeDescriptor();
                persistenceUnitDescriptor.getContains().add(typeDescriptor);
            }
            // Create persistence unit properties
            Properties properties = persistenceUnit.getProperties();
            if (properties != null) {
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
                    propertyDescriptor.setName((String) entry.getKey());
                    propertyDescriptor.setValue((String) entry.getValue());
                    persistenceUnitDescriptor.getProperties().add(propertyDescriptor);
                }
            }
            // Add model unit to model descriptor
            persistenceXmlDescriptor.getContains().add(persistenceUnitDescriptor);
        }
        return persistenceXmlDescriptor;
    }
}
