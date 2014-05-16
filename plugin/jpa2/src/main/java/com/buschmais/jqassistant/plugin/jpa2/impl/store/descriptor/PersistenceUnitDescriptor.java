package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * A descriptor for JPA model units.
 */
@Label("PersistenceUnit")
public interface PersistenceUnitDescriptor extends Descriptor, NamedDescriptor, JpaDescriptor {

    @Property("transactionType")
    String getTransactionType();

    void setTransactionType(String name);

    @Property("description")
    String getDescription();

    void setDescription(String description);

    @Property("provider")
    String getProvider();

    void setProvider(String provider);

    @Property("jtaDatasource")
    String getJtaDataSource();

    void setJtaDataSource(String jtaDataSource);

    @Property("nonJtaDatasource")
    String getNonJtaDataSource();

    void setNonJtaDataSource(String nonJtaDataSource);

    @Property("validationMode")
    String getValidationMode();

    void setValidationMode(String validationMode);

    @Relation("CONTAINS")
    Set<TypeDescriptor> getContains();

    @Property("sharedCacheMode")
    String getSharedCacheMode();

    void setSharedCacheMode(String sharedCacheMode);

    @Relation("HAS")
    Set<PropertyDescriptor> getProperties();

}
