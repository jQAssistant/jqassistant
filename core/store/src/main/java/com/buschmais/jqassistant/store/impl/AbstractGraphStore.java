package com.buschmais.jqassistant.store.impl;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.Descriptor;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;
import com.buschmais.jqassistant.store.impl.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.model.ClassDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.FieldDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.MethodDescriptorImpl;
import com.buschmais.jqassistant.store.impl.model.NodeType;
import com.buschmais.jqassistant.store.impl.model.PackageDescriptorImpl;

public abstract class AbstractGraphStore implements Store {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractGraphStore.class);

	protected GraphDatabaseService database;

	protected ExecutionEngine executionEngine;

	protected Index<Node> packageIndex;
	protected Index<Node> classIndex;

	@Override
	public void start() {
		database = startDatabase();
		executionEngine = new ExecutionEngine(database);
		classIndex = database.index().forNodes("classes");
		packageIndex = database.index().forNodes("packages");
	}

	@Override
	public void stop() {
		executionEngine = null;
		stopDatabase(database);
	}

	public GraphDatabaseAPI getDatabaseAPI() {
		if (database == null) {
			throw new IllegalStateException("Store is not started!.");
		}
		return (GraphDatabaseAPI) database;
	}

	@Override
	public PackageDescriptorImpl resolvePackageDescriptor(
			final PackageDescriptor parentPackageDescriptor,
			final String packageName) {
		final Name name = new Name(parentPackageDescriptor, '.', packageName);
		Node node = packageIndex.get(Descriptor.FULLQUALIFIEDNAME,
				name.getFullQualifiedName()).getSingle();
		if (node != null) {
			return new PackageDescriptorImpl(node);
		}
		node = createNode(name.getFullQualifiedName());
		PackageDescriptorImpl packageDescriptor = new PackageDescriptorImpl(
				node);
		initDescriptor(packageDescriptor, name, NodeType.PACKAGE, packageIndex);
		return packageDescriptor;
	}

	@Override
	public ClassDescriptorImpl resolveClassDescriptor(
			final PackageDescriptor packageDescriptor, final String className) {
		final Name name = new Name(packageDescriptor, '.', className);
		Node node = classIndex.get(Descriptor.FULLQUALIFIEDNAME,
				name.getFullQualifiedName()).getSingle();
		if (node != null) {
			return new ClassDescriptorImpl(node);
		}
		node = createNode(name.getFullQualifiedName());
		ClassDescriptorImpl classDescriptor = new ClassDescriptorImpl(node);
		initDescriptor(classDescriptor, name, NodeType.CLASS, classIndex);
		return classDescriptor;
	}

	@Override
	public MethodDescriptorImpl resolveMethodDescriptor(
			final ClassDescriptor classDescriptor, String methodName) {
		final Name name = new Name(classDescriptor, '#', methodName);
		Node methodNode = createNode(name.getFullQualifiedName());
		MethodDescriptorImpl methodDescriptor = new MethodDescriptorImpl(
				methodNode);
		initDescriptor(methodDescriptor, name, NodeType.METHOD, null);
		return methodDescriptor;
	}

	@Override
	public FieldDescriptor resolveFieldDescriptor(
			final ClassDescriptor classDescriptor, String fieldName) {
		final Name name = new Name(classDescriptor, '#', fieldName);
		Node fieldNode = createNode(name.getFullQualifiedName());
		FieldDescriptorImpl fieldDescriptor = new FieldDescriptorImpl(fieldNode);
		initDescriptor(fieldDescriptor, name, NodeType.FIELD, null);
		return fieldDescriptor;
	}

	private Node createNode(String fullQualifiedName) {
		LOGGER.debug("Creating node for '{}'.", fullQualifiedName);
		Node node = database.createNode();
		return node;
	}

	private void initDescriptor(AbstractDescriptor descriptor, Name name,
			NodeType type, Index<Node> index) {
		descriptor.setFullQualifiedName(name.getFullQualifiedName());
		descriptor.setType(type.name().toLowerCase());
		if (index != null) {
			index.add(descriptor.getNode(), Descriptor.FULLQUALIFIEDNAME,
					name.getFullQualifiedName());
		}
	}

	protected abstract GraphDatabaseService startDatabase();

	protected abstract void stopDatabase(GraphDatabaseService database);

}