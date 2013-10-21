package com.buschmais.jqassistant.plugin.java.impl.store.mapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyFileDescriptor;

/**
 * A mapper for properties.
 */
public class PropertyFileDescriptorMapper extends
		AbstractDescriptorMapper<PropertyFileDescriptor, PropertyFileDescriptorMapper.Property, PropertyFileDescriptorMapper.Relation> {

	enum Property {
		FQN;
	}

	enum Relation implements RelationshipType {
		HAS;
	}

	@Override
	public Set<Class<? extends PropertyFileDescriptor>> getJavaType() {
		Set<Class<? extends PropertyFileDescriptor>> javaTypes = new HashSet<>();
		javaTypes.add(PropertyFileDescriptor.class);
		return javaTypes;
	}

	@Override
	public IndexedLabel getPrimaryLabel() {
		return JavaLabel.PROPERTIES;
	}

	@Override
	public PropertyFileDescriptor createInstance(Class<? extends PropertyFileDescriptor> type) {
		return new PropertyFileDescriptor();
	}

	@Override
	public Class<? extends PropertyFileDescriptor> getType(Set<Label> labels) {
		return PropertyFileDescriptor.class;
	}

	@Override
	protected Class<Relation> getRelationKeys() {
		return Relation.class;
	}

	@Override
	protected Class<Property> getPropertyKeys() {
		return Property.class;
	}

	@Override
	protected Set<? extends Descriptor> getRelation(PropertyFileDescriptor descriptor, Relation relation) {
		switch (relation) {
		case HAS:
			return descriptor.getProperties();
		default:
			break;
		}
		return null;
	}

	@Override
	protected void setRelation(PropertyFileDescriptor descriptor, Relation relation, Set<? extends Descriptor> targets) {
		switch (relation) {
		case HAS:
			descriptor.setProperties((Set<PropertyDescriptor>) targets);
		default:
			break;
		}
	}

	@Override
	protected Object getProperty(PropertyFileDescriptor descriptor, Property property) {
		switch (property) {
		case FQN:
			return descriptor.getFullQualifiedName();
		default:
			break;
		}
		return null;
	}

	@Override
	protected void setProperty(PropertyFileDescriptor descriptor, Property property, Object value) {
		switch (property) {
		case FQN:
			descriptor.setFullQualifiedName((String) value);
			break;
		default:
			break;
		}
	}

	@Override
	public Set<? extends Label> getLabels(PropertyFileDescriptor descriptor) {
		return Collections.emptySet();
	}

	@Override
	public void setLabel(PropertyFileDescriptor descriptor, Label label) {
	}
}
