package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.impl.dao.mapper.NodeLabel.PACKAGE;

/**
 * A store for {@link PackageDescriptor}s.
 */
public class PackageDescriptorMapper extends AbstractDescriptorMapper<PackageDescriptor, PackageDescriptorMapper.Property, PackageDescriptorMapper.Relation> {

    enum Property {
        SIGNATURE;
    }

    enum Relation implements RelationshipType {
        CONTAINS;
    }

    @Override
    public Set<Class<? extends PackageDescriptor>> getJavaType() {
        Set<Class<? extends PackageDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(PackageDescriptor.class);
        return javaTypes;
    }

    @Override
    public PrimaryLabel getPrimaryLabel() {
        return PACKAGE;
    }

    @Override
    protected Class<Property> getPropertyKeys() {
        return Property.class;
    }

    @Override
    protected Class<Relation> getRelationKeys() {
        return Relation.class;
    }

    @Override
    public Class<? extends PackageDescriptor> getType(Set<Label> labels) {
        return PackageDescriptor.class;
    }

    @Override
    public PackageDescriptor createInstance(Class<? extends PackageDescriptor> type) {
        return new PackageDescriptor();
    }

    @Override
    public Set<? extends Descriptor> getRelation(PackageDescriptor descriptor, Relation relation) {
        switch (relation) {
            case CONTAINS:
                return descriptor.getContains();
            default:
                break;
        }
        return null;
    }

    @Override
    protected void setRelation(PackageDescriptor descriptor, Relation relation, Set<? extends Descriptor> target) {
        switch (relation) {
            case CONTAINS:
                descriptor.setContains((Set<Descriptor>) target);
                break;
            default:
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(PackageDescriptor descriptor, Property property) {
        switch (property) {
            case SIGNATURE:
                return descriptor.getSignature();
        }
        return null;
    }

    @Override
    public void setProperty(PackageDescriptor descriptor, Property property, Object value) {
        switch (property) {
            case SIGNATURE:
                descriptor.setSignature((String) value);
                break;
            default:
                break;
        }
    }
}
