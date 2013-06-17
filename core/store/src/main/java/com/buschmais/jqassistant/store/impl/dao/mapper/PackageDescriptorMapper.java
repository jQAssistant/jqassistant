package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.store.api.DescriptorDAO.CoreLabel;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public class PackageDescriptorMapper extends
		AbstractDescriptorMapper<PackageDescriptor> {

	@Override
	public Class<PackageDescriptor> getJavaType() {
		return PackageDescriptor.class;
	}

	@Override
	public CoreLabel getCoreLabel() {
		return CoreLabel.PACKAGE;
	}

	@Override
	public PackageDescriptor createInstance() {
		return new PackageDescriptor();
	}

	@Override
	public Map<RelationType, Set<? extends AbstractDescriptor>> getRelations(
			PackageDescriptor descriptor) {
		Map<RelationType, Set<? extends AbstractDescriptor>> relations = new HashMap<RelationType, Set<? extends AbstractDescriptor>>();
		relations.put(RelationType.CONTAINS, descriptor.getContains());
		return relations;
	}

	@Override
	protected void setRelation(PackageDescriptor descriptor,
			RelationType relation, AbstractDescriptor target) {
		switch (relation) {
		case CONTAINS:
			descriptor.getContains().add(target);
			break;
		default:
			throw new IllegalArgumentException("Unsupported relation type "
					+ relation);
		}
	}

}
