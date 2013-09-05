/**
 *
 */
package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.api.model.NodeLabel.ARTIFACT;

/**
 * Maps the {@link ArtifactDescriptor} to nodes and relationships.
 *
 * @author Herklotz
 */
public class ArtifactDescriptorMapper extends AbstractDescriptorMapper<ArtifactDescriptor> {

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
    public Map<NodeProperty, Object> getProperties(ArtifactDescriptor descriptor) {
        Map<NodeProperty, Object> properties = super.getProperties(descriptor);
        properties.put(NodeProperty.GROUP, descriptor.getGroup());
        properties.put(NodeProperty.NAME, descriptor.getName());
        properties.put(NodeProperty.VERSION, descriptor.getVersion());
        properties.put(NodeProperty.CLASSIFIER, descriptor.getClassifier());
        properties.put(NodeProperty.TYPE, descriptor.getType());
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(ArtifactDescriptor descriptor, NodeProperty property, Object value) {
        super.setProperty(descriptor, property, value);
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
    public Map<Relation, Set<? extends Descriptor>> getRelations(ArtifactDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<Relation, Set<? extends Descriptor>>();
        relations.put(Relation.CONTAINS, descriptor.getContains());
        return relations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setRelation(ArtifactDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case CONTAINS:
                descriptor.getContains().add(target);
                break;
            default:
        }
    }
}
