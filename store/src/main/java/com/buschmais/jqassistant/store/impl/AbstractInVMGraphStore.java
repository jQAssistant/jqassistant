package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.model.Descriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;
import com.buschmais.jqassistant.store.impl.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.model.AbstractParentDescriptor;
import com.buschmais.jqassistant.store.impl.model.ClassDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.MethodDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.PackageDescriptorImpl;

public abstract class AbstractInVMGraphStore extends AbstractGraphStore {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractInVMGraphStore.class);

	private Transaction transaction = null;

	private Index<Node> packageIndex;
	private Index<Node> classIndex;
	private Index<Node> methodIndex;

	@Override
	public void beginTransaction() {
		if (transaction != null) {
			throw new IllegalStateException(
					"There is already an existing transaction.");
		}
		transaction = database.beginTx();
		methodIndex = database.index().forNodes("methods");
		classIndex = database.index().forNodes("classes");
		packageIndex = database.index().forNodes("packages");
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
		Node node = classIndex.get(Descriptor.FULLQUALIFIEDNAME,
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
		Node classNode = createNode(fullQualifiedName);
		ClassDescriptorImpl classDescriptor = new ClassDescriptorImpl(
				classNode, packageDescriptor);
		initDescriptor(classDescriptor, packageDescriptor, name, classIndex);
		return classDescriptor;
	}

	@Override
	public PackageDescriptorImpl getPackageDescriptor(String fullQualifiedName) {
		Node node = packageIndex.get(Descriptor.FULLQUALIFIEDNAME,
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
		Node packageNode = createNode(fullQualifiedName);
		PackageDescriptorImpl packageDescriptor = new PackageDescriptorImpl(
				packageNode, parentPackageDescriptor);
		initDescriptor(packageDescriptor, parentPackageDescriptor, name,
				packageIndex);
		return packageDescriptor;
	}

	@Override
	public MethodDescriptor getMethodDescriptor(String fullQualifiedName) {
		Node node = packageIndex.get(Descriptor.FULLQUALIFIEDNAME,
				fullQualifiedName).getSingle();
		if (node != null) {
			Name name = getName(fullQualifiedName, '#');
			ClassDescriptorImpl classDescriptor = getClassDescriptor(name
					.getParentName());
			return new MethodDescriptorImpl(node, classDescriptor);
		}
		return null;
	}

	@Override
	public MethodDescriptor createMethodDescriptor(String fullQualifiedName) {
		Name name = getName(fullQualifiedName, '#');
		ClassDescriptorImpl classDescriptor = getClassDescriptor(name
				.getParentName());
		Node methodNode = createNode(fullQualifiedName);
		MethodDescriptorImpl methodDescriptor = new MethodDescriptorImpl(
				methodNode, classDescriptor);
		initDescriptor(methodDescriptor, classDescriptor, name, methodIndex);
		return methodDescriptor;
	}

	private Node createNode(String fullQualifiedName) {
		LOGGER.info("Creating node for '{}'.", fullQualifiedName);
		Node node = database.createNode();
		return node;
	}

	private PackageDescriptorImpl resolvePackageDescriptor(
			String fullQualifiedName) {
		PackageDescriptorImpl packageDescriptor = null;
		if (fullQualifiedName != null
				&& getPackageDescriptor(fullQualifiedName) == null) {
			packageDescriptor = createPackageDescriptor(fullQualifiedName);
		}
		return packageDescriptor;
	}

	private void initDescriptor(AbstractDescriptor descriptor,
			AbstractParentDescriptor parent, Name name, Index<Node> index) {
		descriptor.setFullQualifiedName(name.getFullQualifiedName());
		descriptor.setLocalName(name.getLocalName());
		index.add(descriptor.getNode(), Descriptor.FULLQUALIFIEDNAME,
				name.getFullQualifiedName());
		if (parent != null) {
			parent.addChild(descriptor);
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

}