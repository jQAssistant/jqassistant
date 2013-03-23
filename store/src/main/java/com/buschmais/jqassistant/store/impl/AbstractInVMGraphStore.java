package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.model.Descriptor;
import com.buschmais.jqassistant.store.impl.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.model.ClassDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.PackageDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public abstract class AbstractInVMGraphStore extends AbstractGraphStore {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractInVMGraphStore.class);

	private Transaction transaction = null;

	@Override
	public void beginTransaction() {
		if (transaction != null) {
			throw new IllegalStateException(
					"There is already an existing transaction.");
		}
		transaction = database.beginTx();

	}

	@Override
	public void endTransaction() {
		if (transaction == null) {
			throw new IllegalStateException("There is no existing transaction.");
		}
		transaction.success();
		transaction.finish();
		transaction = null;
	}

	@Override
	public ClassDescriptorImpl getClassDescriptor(String fullQualifiedName) {
		Node node = getClassIndex().get(Descriptor.FULLQUALIFIEDNAME,
				fullQualifiedName).getSingle();
		if (node != null) {
			Name name = getName(fullQualifiedName, '.');
			PackageDescriptorImpl packageDescriptor = name.getParentName() != null ? getPackageDescriptor(name
					.getParentName()) : null;
			return new ClassDescriptorImpl(node, packageDescriptor);
		}
		return null;
	}

	@Override
	public ClassDescriptorImpl createClassDescriptor(String fullQualifiedName) {
		Name name = getName(fullQualifiedName, '.');
		PackageDescriptorImpl packageDescriptor = resolvePackageDescriptor(name
				.getParentName());
		LOGGER.info("Creating node for '{}'.", fullQualifiedName);
		Node classNode = database.createNode();
		ClassDescriptorImpl classDescriptor = new ClassDescriptorImpl(
				classNode, packageDescriptor);
		initDescriptor(classDescriptor, packageDescriptor, name,
				getClassIndex());
		return classDescriptor;
	}

	@Override
	public PackageDescriptorImpl getPackageDescriptor(String fullQualifiedName) {
		Node node = getPackageIndex().get(Descriptor.FULLQUALIFIEDNAME,
				fullQualifiedName).getSingle();
		if (node != null) {
			Name name = getName(fullQualifiedName, '.');
			PackageDescriptorImpl parentPackageDescriptor = name
					.getParentName() != null ? getPackageDescriptor(name
					.getParentName()) : null;
			return new PackageDescriptorImpl(node, parentPackageDescriptor);
		}
		return null;
	}

	public PackageDescriptorImpl createPackageDescriptor(
			String fullQualifiedName) {
		Name name = getName(fullQualifiedName, '.');
		PackageDescriptorImpl parentPackageDescriptor = resolvePackageDescriptor(name
				.getParentName());
		LOGGER.info("Creating node for '{}'.", fullQualifiedName);
		Node packageNode = database.createNode();
		PackageDescriptorImpl packageDescriptor = new PackageDescriptorImpl(
				packageNode, parentPackageDescriptor);
		initDescriptor(packageDescriptor, parentPackageDescriptor, name,
				getPackageIndex());
		return packageDescriptor;
	}

	private PackageDescriptorImpl resolvePackageDescriptor(String name) {
		PackageDescriptorImpl parentPackageDescriptor = null;
		if (name != null) {
			parentPackageDescriptor = getPackageDescriptor(name);
			if (parentPackageDescriptor == null) {
				parentPackageDescriptor = createPackageDescriptor(name);
			}
		}
		return parentPackageDescriptor;
	}

	private void initDescriptor(AbstractDescriptor descriptor,
			AbstractDescriptor parent, Name name, Index<Node> index) {
		descriptor.setFullQualifiedName(name.getFullQualifiedName());
		descriptor.setLocalName(name.getLocalName());
		getClassIndex().add(descriptor.getNode(), Descriptor.FULLQUALIFIEDNAME,
				name.getFullQualifiedName());
		if (parent != null) {
			parent.getNode().createRelationshipTo(descriptor.getNode(),
					RelationType.CONTAINS);
		}
	}

	private Name getName(String fullQualifiedName, char localNameSeparator) {
		int n = fullQualifiedName.lastIndexOf(localNameSeparator);
		String localName;
		String parentName;
		if (n > -1) {
			localName = fullQualifiedName.substring(n + 1,
					fullQualifiedName.length());
			parentName = fullQualifiedName.substring(0, n);
		} else {
			localName = fullQualifiedName;
			parentName = null;
		}
		return new Name(parentName, localName, fullQualifiedName);
	}

	private Index<Node> getClassIndex() {
		return database.index().forNodes("classes");
	}

	private Index<Node> getPackageIndex() {
		return database.index().forNodes("packages");
	}

}