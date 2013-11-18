package com.buschmais.jqassistant.core.store.impl;

import static com.buschmais.cdo.api.Query.Result;
import static com.buschmais.cdo.api.Query.Result.CompositeRowObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.cdo.api.CdoManager;
import com.buschmais.cdo.api.CdoManagerFactory;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

/**
 * Abstract base implementation of a {@link Store}.
 * <p>
 * Provides methods for managing the life packages of a store, transactions,
 * resolving descriptors and executing CYPHER queries.
 * </p>
 */
public abstract class AbstractGraphStore implements Store {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphStore.class);

	private CdoManagerFactory cdoManagerFactory;

	private CdoManager cdoManager;

	private Map<String, FullQualifiedNameDescriptor> fqnCache;

	@Override
	public void start(Collection<Class<?>> types) {
		cdoManagerFactory = createCdoManagerFactory(types);
		cdoManager = cdoManagerFactory.createCdoManager();
		fqnCache = new LRUMap(65536);
	}

	@Override
	public void stop() {
		if (cdoManager != null) {
			cdoManager.close();
		}
		if (cdoManagerFactory != null) {
			closeCdoManagerFactory(cdoManagerFactory);
		}
	}

	@Override
	public <T extends Descriptor> T create(Class<T> type) {
		T descriptor = cdoManager.create(type);
		return descriptor;
	}

	@Override
	public <T extends FullQualifiedNameDescriptor> T create(Class<T> type, String fullQualifiedName) {
		T descriptor = cdoManager.create(type);
		descriptor.setFullQualifiedName(fullQualifiedName);
		fqnCache.put(fullQualifiedName, descriptor);
		return descriptor;
	}

	@Override
	public <T extends Descriptor, C extends T> C migrate(T descriptor, Class<C> concreteType) {
		if (descriptor instanceof FullQualifiedNameDescriptor) {
			fqnCache.remove(((FullQualifiedNameDescriptor) descriptor).getFullQualifiedName());
		}
		C migrated = cdoManager.migrate(descriptor, concreteType);
		if (migrated instanceof FullQualifiedNameDescriptor) {
			fqnCache.put(((FullQualifiedNameDescriptor) migrated).getFullQualifiedName(), (FullQualifiedNameDescriptor) migrated);
		}
		return migrated;
	}

	@Override
	public <T extends Descriptor> T find(Class<T> type, String fullQualifiedName) {
		T t = (T) fqnCache.get(fullQualifiedName);
		if (t != null) {
			return t;
		}
		Iterable<T> ts = cdoManager.find(type, fullQualifiedName);
		Iterator<T> iterator = ts.iterator();
		if (iterator.hasNext()) {
			t = iterator.next();
		}
		if (iterator.hasNext()) {
			throw new IllegalArgumentException("Found more than one descriptors for " + type.getName() + " and name " + fullQualifiedName);
		}
		return t;
	}

	@Override
	public <QL> Result<CompositeRowObject> executeQuery(QL query, Map<String, Object> parameters) {
		return cdoManager.createQuery(query).withParameters(parameters).execute();
	}

	@Override
	public void reset() {
		LOGGER.info("Resetting store.");
		beginTransaction();
		cdoManager.createQuery("MATCH (n)-[r]-(d) DELETE r").execute();
		cdoManager.createQuery("MATCH (n) DELETE n").execute();
		commitTransaction();
		LOGGER.info("Reset finished.");
	}

	@Override
	public void beginTransaction() {
		cdoManager.begin();
	}

	@Override
	public void commitTransaction() {
		cdoManager.commit();
	}

	@Override
	public void rollbackTransaction() {
		cdoManager.rollback();
	}

	public GraphDatabaseAPI getDatabaseService() {
		return getDatabaseAPI(cdoManager);
	}

	protected abstract GraphDatabaseAPI getDatabaseAPI(CdoManager cdoManager);

	/**
	 * Delegates to the sub class to start the database.
	 * 
	 * @return The {@link GraphDatabaseService} instance to use.
	 */
	protected abstract CdoManagerFactory createCdoManagerFactory(Collection<Class<?>> types);

	/**
	 * Delegates to the sub class to stop the database.
	 * 
	 * @param database
	 *            The used {@link GraphDatabaseService} instance.
	 */
	protected abstract void closeCdoManagerFactory(CdoManagerFactory database);

}
