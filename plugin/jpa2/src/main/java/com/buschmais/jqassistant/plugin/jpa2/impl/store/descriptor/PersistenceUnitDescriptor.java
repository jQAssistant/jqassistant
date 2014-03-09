package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

import java.util.Set;

/**
 * A descriptor for JPA model units.
 */
@Label("PERSISTENCEUNIT")
public interface PersistenceUnitDescriptor extends Descriptor, NamedDescriptor, JpaDescriptor {

    @Property("DESCRIPTION")
    public String getDescription();

    public void setDescription(String description);

    @Property("PROVIDER")
    public String getProvider();

    public void setProvider(String provider);

    @Property("JTADATASOURCE")
    public String getJtaDataSource();

    public void setJtaDataSource(String jtaDataSource);

    @Property("NONJTADATASOURCE")
    public String getNonJtaDataSource();

    public void setNonJtaDataSource(String nonJtaDataSource);

    @Property("VALIDATIONMODE")
    public String getValidationMode();

    public void setValidationMode(String validationMode);

    @Relation("CONTAINS")
    public Set<TypeDescriptor> getContains();

    @Property("SHAREDCACHEMODE")
    public String getSharedCacheMode();

    public void setSharedCacheMode(String sharedCacheMode);

    @Property("PROPERTIES")
    public Set<PropertyDescriptor> getProperties();

}
