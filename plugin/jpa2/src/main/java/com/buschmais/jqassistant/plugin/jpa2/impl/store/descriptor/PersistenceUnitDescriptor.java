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

    @Property("description")
    public String getDescription();

    public void setDescription(String description);

    @Property("provider")
    public String getProvider();

    public void setProvider(String provider);

    @Property("jtaDatasource")
    public String getJtaDataSource();

    public void setJtaDataSource(String jtaDataSource);

    @Property("nonJtaDatasource")
    public String getNonJtaDataSource();

    public void setNonJtaDataSource(String nonJtaDataSource);

    @Property("validationMode")
    public String getValidationMode();

    public void setValidationMode(String validationMode);

    @Relation("Contains")
    public Set<TypeDescriptor> getContains();

    @Property("sharedCacheMode")
    public String getSharedCacheMode();

    public void setSharedCacheMode(String sharedCacheMode);

    @Relation("HAS")
    public Set<PropertyDescriptor> getProperties();

}
