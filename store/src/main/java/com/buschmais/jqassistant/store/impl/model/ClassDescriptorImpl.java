package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public class ClassDescriptorImpl extends AbstractDescriptor implements
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
		getNode().createRelationshipTo(getNode(dependency),
				RelationType.DEPENDS_ON);
	}

	@Override
	public void addSuperClass(ClassDescriptor superClass) {
		getNode().createRelationshipTo(getNode(superClass),
				RelationType.INHERITS_FROM);

	}

	@Override
	public void addImplements(ClassDescriptor interfaceClass) {
		getNode().createRelationshipTo(getNode(interfaceClass),
				RelationType.IMPLEMENTS);

	}
}
