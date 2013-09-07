/**
 *
 */
package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.impl.dao.mapper.NodeLabel.ARTIFACT;

/**
 * Maps the {@link ArtifactDescriptor} to nodes and relationships.
 *
 * @author Herklotz
 */
public class ArtifactDescriptorMapper extends AbstractDescriptorMapper<ArtifactDescriptor, ArtifactDescriptorMapper.Property, ArtifactDescriptorMapper.Relation> {

    enum Property {
        GROUP,
        NAME,
        VERSION,
        CLASSIFIER,
        TYPE;
    }

    enum Relation implements RelationshipType {
        CONTAINS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends ArtifactDescriptor>> getJavaType() {
        Set<Class<? extends ArtifactDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(ArtifactDescriptor.class);
        return javaTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrimaryLabel getPrimaryLabel() {
        return ARTIFACT;
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
    public Class<? extends ArtifactDescriptor> getType(Set<Label> labels) {
        return ArtifactDescriptor.class;
    }

    @Override
    public ArtifactDescriptor createInstance(Class<? extends ArtifactDescriptor> type) {
        return new ArtifactDescriptor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(ArtifactDescriptor descriptor, Property property) {
        switch (property) {
            case GROUP:
                return descriptor.getGroup();
            case NAME:
                return descriptor.getName();
            case VERSION:
                return descriptor.getVersion();
            case CLASSIFIER:
                return descriptor.getClassifier();
            case TYPE:
                return descriptor.getType();
            default:
                break;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(ArtifactDescriptor descriptor, Property property, Object value) {
        switch (property) {
            case GROUP:
                descriptor.setGroup((String) value);
                break;
            case NAME:
                descriptor.setName((String) value);
                break;
            case VERSION:
                descriptor.setVersion((String) value);
                break;
            case CLASSIFIER:
                descriptor.setClassifier((String) value);
            case TYPE:
                descriptor.setType((String) value);
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<? extends Descriptor> getRelation(ArtifactDescriptor descriptor, Relation relation) {
        switch (relation) {
            case CONTAINS:
                return descriptor.getContains();
            default:
                break;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setRelation(ArtifactDescriptor descriptor, Relation relation, Set<?extends Descriptor> target) {
        switch (relation) {
            case CONTAINS:
                descriptor.setContains((Set<Descriptor>) target);
                break;
            default:
        }
    }
}
