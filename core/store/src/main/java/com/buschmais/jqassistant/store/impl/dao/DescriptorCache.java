package com.buschmais.jqassistant.store.impl.dao;

import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;

public class DescriptorCache {

	@SuppressWarnings("unchecked")
	private final Map<Long, CacheEntry<? extends AbstractDescriptor>> nodeCache = new LRUMap(
			2048);

	private static final class CacheEntry<T extends AbstractDescriptor> {
		private final T descriptor;
		private final Node node;

		public CacheEntry(T descriptor, Node node) {
			super();
			this.descriptor = descriptor;
			this.node = node;
		}

		public T getDescriptor() {
			return descriptor;
		}

		public Node getNode() {
			return node;
		}
	}

	public <T extends AbstractDescriptor> void put(T descriptor, Node node) {
		Long key = Long.valueOf(node.getId());
		this.nodeCache.put(key, new CacheEntry<T>(descriptor, node));
	}

	public <T extends AbstractDescriptor> T findBy(Node node) {
		@SuppressWarnings("unchecked")
		CacheEntry<T> cacheEntry = (CacheEntry<T>) nodeCache.get(Long
				.valueOf(node.getId()));
		return cacheEntry != null ? cacheEntry.getDescriptor() : null;
	}

	public Node findBy(Long id) {
		CacheEntry<? extends AbstractDescriptor> cacheEntry = nodeCache.get(id);
		return cacheEntry != null ? cacheEntry.getNode() : null;
	}

}
