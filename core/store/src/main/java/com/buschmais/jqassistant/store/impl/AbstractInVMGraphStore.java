package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.model.Descriptor;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;
import com.buschmais.jqassistant.store.impl.DescriptorCache.DescriptorFactory;
import com.buschmais.jqassistant.store.impl.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.model.AbstractParentDescriptor;
import com.buschmais.jqassistant.store.impl.model.ClassDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.FieldDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.MethodDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.NodeType;
import com.buschmais.jqassistant.store.impl.model.PackageDescriptorImpl;

public abstract class AbstractInVMGraphStore extends AbstractGraphStore {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractInVMGraphStore.class);

	private Transaction transaction = null;

	private DescriptorCache<PackageDescriptorImpl> packageCache;
	private Index<Node> packageIndex;
	private DescriptorCache<ClassDescriptorImpl> classCache;
	private Index<Node> classIndex;
	private DescriptorCache<MethodDescriptorImpl> methodCache;
	private DescriptorCache<FieldDescriptorImpl> fieldCache;

	@Override
	public void beginTransaction() {
		if (transaction != null) {
			throw new IllegalStateException(
					"There is already an existing transaction.");
		}
		transaction = database.beginTx();
		fieldCache = new DescriptorCache<FieldDescriptorImpl>();
		methodCache = new DescriptorCache<MethodDescriptorImpl>();
		classIndex = database.index().forNodes("classes");
		classCache = new DescriptorCache<ClassDescriptorImpl>();
		packageIndex = database.index().forNodes("packages");
		packageCache = new DescriptorCache<PackageDescriptorImpl>();
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
	public ClassDescriptorImpl resolveClassDescriptor(
			final String fullQualifiedName) {
		return classCache.get(fullQualifiedName,
				new DescriptorFactory<ClassDescriptorImpl>() {

					@Override
					public ClassDescriptorImpl create() {
						Node node = classIndex
								.get(Descriptor.FULLQUALIFIEDNAME,
										fullQualifiedName).getSingle();
						if (node != null) {
							return new ClassDescriptorImpl(node);
						}
						Name name = getName(fullQualifiedName, '.');
						PackageDescriptorImpl packageDescriptor = null;
						if (name.getParentName() != null) {
							packageDescriptor = resolvePackageDescriptor(name
									.getParentName());
						}
						Node classNode = createNode(fullQualifiedName);
						ClassDescriptorImpl classDescriptor = new ClassDescriptorImpl(
								classNode);
						initDescriptor(classDescriptor, packageDescriptor,
								name, NodeType.CLASS, classIndex);
						return classDescriptor;
					}
				});
	}

	@Override
	public PackageDescriptorImpl resolvePackageDescriptor(
			final String fullQualifiedName) {
		return packageCache.get(fullQualifiedName,
				new DescriptorFactory<PackageDescriptorImpl>() {

					@Override
					public PackageDescriptorImpl create() {
						Name name = getName(fullQualifiedName, '.');
						PackageDescriptorImpl parentPackageDescriptor = null;
						if (name.getParentName() != null) {
							parentPackageDescriptor = resolvePackageDescriptor(name
									.getParentName());
						}
						Node node = packageIndex
								.get(Descriptor.FULLQUALIFIEDNAME,
										fullQualifiedName).getSingle();
						if (node != null) {
							return new PackageDescriptorImpl(node,
									parentPackageDescriptor);
						}
						Node packageNode = createNode(fullQualifiedName);
						PackageDescriptorImpl packageDescriptor = new PackageDescriptorImpl(
								packageNode, parentPackageDescriptor);
						initDescriptor(packageDescriptor,
								parentPackageDescriptor, name,
								NodeType.PACKAGE, packageIndex);
						return packageDescriptor;
					}
				});
	}

	@Override
	public MethodDescriptorImpl resolveMethodDescriptor(
			final String fullQualifiedName) {
		return methodCache.get(fullQualifiedName,
				new DescriptorFactory<MethodDescriptorImpl>() {

					@Override
					public MethodDescriptorImpl create() {
						Name name = getName(fullQualifiedName, '#');
						ClassDescriptorImpl classDescriptor = resolveClassDescriptor(name
								.getParentName());
						Node methodNode = createNode(fullQualifiedName);
						MethodDescriptorImpl methodDescriptor = new MethodDescriptorImpl(
								methodNode);
						initDescriptor(methodDescriptor, classDescriptor, name,
								NodeType.METHOD, null);
						return methodDescriptor;
					}
				});
	}

	@Override
	public FieldDescriptor resolveFieldDescriptor(final String fullQualifiedName) {
		return fieldCache.get(fullQualifiedName,
				new DescriptorFactory<FieldDescriptorImpl>() {

					@Override
					public FieldDescriptorImpl create() {
						Name name = getName(fullQualifiedName, '#');
						ClassDescriptorImpl classDescriptor = resolveClassDescriptor(name
								.getParentName());
						Node fieldNode = createNode(fullQualifiedName);
						FieldDescriptorImpl fieldDescriptor = new FieldDescriptorImpl(
								fieldNode);
						initDescriptor(fieldDescriptor, classDescriptor, name,
								NodeType.FIELD, null);
						return fieldDescriptor;
					}
				});
	}

	private Node createNode(String fullQualifiedName) {
		LOGGER.debug("Creating node for '{}'.", fullQualifiedName);
		Node node = database.createNode();
		return node;
	}

	private void initDescriptor(AbstractDescriptor descriptor,
			AbstractParentDescriptor parent, Name name, NodeType type,
			Index<Node> index) {
		descriptor.setFullQualifiedName(name.getFullQualifiedName());
		descriptor.setType(type.name().toLowerCase());
		if (index != null) {
			index.add(descriptor.getNode(), Descriptor.FULLQUALIFIEDNAME,
					name.getFullQualifiedName());
		}
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