package com.buschmais.jqassistant.store.impl.dao;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.DescriptorDAO.NodeProperty;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;
import com.buschmais.jqassistant.store.impl.model.NodeType;

public class DescriptorAdapterRegistry {

	private final Map<Class<? extends AbstractDescriptor>, DescriptorMapper<?>> adaptersByJavaType = new HashMap<Class<? extends AbstractDescriptor>, DescriptorMapper<?>>();
	private final Map<NodeType, DescriptorMapper<?>> adaptersByNodeType = new HashMap<NodeType, DescriptorMapper<?>>();

	public void registerDAO(
			DescriptorMapper<? extends AbstractDescriptor> adapter) {
		this.adaptersByJavaType.put(adapter.getJavaType(), adapter);
		this.adaptersByNodeType.put(adapter.getNodeType(), adapter);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractDescriptor> DescriptorMapper<T> getDescriptorAdapter(
			Node node) {
		NodeType nodeType = NodeType.valueOf((String) node
				.getProperty(NodeProperty.TYPE.name()));
		DescriptorMapper<T> adapter = (DescriptorMapper<T>) adaptersByNodeType
				.get(nodeType);
		if (adapter == null) {
			throw new IllegalArgumentException(
					"Cannot find adapter for node type " + nodeType);
		}
		return adapter;
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractDescriptor> DescriptorMapper<T> getDescriptorAdapter(
			Class<?> javaType) {
		DescriptorMapper<T> adapter = (DescriptorMapper<T>) adaptersByJavaType
				.get(javaType);
		if (adapter == null) {
			throw new IllegalArgumentException(
					"Cannot find adapter for java type " + javaType);
		}
		return adapter;
	}
}
