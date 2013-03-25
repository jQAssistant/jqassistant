package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public class ClassDescriptorImpl extends AbstractParentDescriptor implements
		ClassDescriptor {

	private final PackageDescriptorImpl packageDescriptor;

	public ClassDescriptorImpl(Node node,
			PackageDescriptorImpl packageDescriptor) {
		super(node);
		this.packageDescriptor = packageDescriptor;
	}

	public PackageDescriptor getPackageDescriptor() {
		return packageDescriptor;
	}

	@Override
	public void addDependency(ClassDescriptor dependency) {
		addRelationShip(RelationType.DEPENDS_ON, dependency);
	}

	@Override
	public void addSuperClass(ClassDescriptor superClass) {
		addRelationShip(RelationType.INHERITS_FROM, superClass);

	}

	@Override
	public void addImplements(ClassDescriptor interfaceClass) {
		addRelationShip(RelationType.IMPLEMENTS, interfaceClass);
	}

}
