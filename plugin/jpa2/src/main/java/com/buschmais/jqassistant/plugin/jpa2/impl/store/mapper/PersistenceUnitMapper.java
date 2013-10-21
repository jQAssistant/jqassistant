package com.buschmais.jqassistant.plugin.jpa2.impl.store.mapper;

import static com.buschmais.jqassistant.plugin.jpa2.impl.store.mapper.Jpa2Label.JPA;
import static com.buschmais.jqassistant.plugin.jpa2.impl.store.mapper.Jpa2Label.PERSISTENCEUNIT;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor.PersistenceUnitDescriptor;

/**
 * {@link AbstractDescriptorMapper} for {@PersistenceUnitDescriptor
 * 
 * 
 * }s.
 */
public class PersistenceUnitMapper
		extends
		AbstractDescriptorMapper<PersistenceUnitDescriptor, PersistenceUnitMapper.PersistenceUnitProperty, PersistenceUnitMapper.PersistenceUnitRelation> {

	enum PersistenceUnitProperty {
		NAME, DESCRIPTION, PROVIDER, JTADATASOURCE, NONJTADATASOURCE, VALIDATIONMODE, SHAREDCACHEMODE;
	}

	enum PersistenceUnitRelation implements RelationshipType {
		CONTAINS, HAS;
	}

	@Override
	public Set<Class<? extends PersistenceUnitDescriptor>> getJavaType() {
		Set<Class<? extends PersistenceUnitDescriptor>> javaTypes = new HashSet<>();
		javaTypes.add(PersistenceUnitDescriptor.class);
		return javaTypes;
	}

	@Override
	protected Class<PersistenceUnitProperty> getPropertyKeys() {
		return PersistenceUnitProperty.class;
	}

	@Override
	protected Class<PersistenceUnitRelation> getRelationKeys() {
		return PersistenceUnitRelation.class;
	}

	@Override
	public PersistenceUnitDescriptor createInstance(Class<? extends PersistenceUnitDescriptor> type) {
		return new PersistenceUnitDescriptor();
	}

	@Override
	public Class<? extends PersistenceUnitDescriptor> getType(Set<Label> labels) {
		return PersistenceUnitDescriptor.class;
	}

	@Override
	public IndexedLabel getPrimaryLabel() {
		return PERSISTENCEUNIT;
	}

	@Override
	public void setProperty(PersistenceUnitDescriptor descriptor, PersistenceUnitProperty property, Object value) {
		switch (property) {
		case NAME:
			descriptor.setName((String) value);
		case DESCRIPTION:
			descriptor.setDescription((String) value);
		case JTADATASOURCE:
			descriptor.setJtaDataSource((String) value);
		case NONJTADATASOURCE:
			descriptor.setNonJtaDataSource((String) value);
		case PROVIDER:
			descriptor.setProvider((String) value);
		case VALIDATIONMODE:
			descriptor.setValidationMode((String) value);
		case SHAREDCACHEMODE:
			descriptor.setSharedCacheMode((String) value);
		default:
			break;
		}
	}

	@Override
	protected Object getProperty(PersistenceUnitDescriptor descriptor, PersistenceUnitProperty property) {
		switch (property) {
		case NAME:
			return descriptor.getName();
		case DESCRIPTION:
			return descriptor.getDescription();
		case JTADATASOURCE:
			return descriptor.getJtaDataSource();
		case NONJTADATASOURCE:
			return descriptor.getNonJtaDataSource();
		case PROVIDER:
			return descriptor.getProvider();
		case VALIDATIONMODE:
			return descriptor.getValidationMode();
		case SHAREDCACHEMODE:
			return descriptor.getSharedCacheMode();
		default:
			break;
		}
		return null;
	}

	@Override
	protected void setRelation(PersistenceUnitDescriptor descriptor, PersistenceUnitRelation relation, Set<? extends Descriptor> target) {
		switch (relation) {
		case CONTAINS:
			descriptor.setContains((Set<TypeDescriptor>) target);
			break;
		case HAS:
			descriptor.setProperties((Set<PropertyDescriptor>) target);
		default:
			break;
		}
	}

	@Override
	public Set<? extends Descriptor> getRelation(PersistenceUnitDescriptor descriptor, PersistenceUnitRelation relation) {
		switch (relation) {
		case CONTAINS:
			return descriptor.getContains();
		case HAS:
			return descriptor.getProperties();
		default:
			break;
		}
		return null;
	}

	@Override
	public Set<? extends Label> getLabels(PersistenceUnitDescriptor descriptor) {
		return asSet(JPA);
	}
}
