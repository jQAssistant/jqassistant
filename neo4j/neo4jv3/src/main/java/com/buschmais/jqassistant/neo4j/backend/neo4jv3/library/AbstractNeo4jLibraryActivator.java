package com.buschmais.jqassistant.neo4j.backend.neo4jv3.library;

import com.buschmais.jqassistant.neo4j.backend.neo4jv3.Neo4jLibraryActivator;

import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNeo4jLibraryActivator implements Neo4jLibraryActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNeo4jLibraryActivator.class);

    public final void register(GraphDatabaseAPI graphDatabaseAPI) {
        Procedures procedures = graphDatabaseAPI.getDependencyResolver().resolveDependency(Procedures.class);
        for (Class<?> procedureType : getProcedureTypes()) {
            try {
                LOGGER.debug("Registering procedure class " + procedureType.getName());
                procedures.registerProcedure(procedureType);
            } catch (KernelException e) {
                LOGGER.warn("Cannot register procedure class " + procedureType.getName(), e);
            }
        }
        for (Class<?> functionType : getFunctionTypes()) {
            try {
                LOGGER.debug("Registering function class " + functionType.getName());
                procedures.registerFunction(functionType);
            } catch (KernelException e) {
                LOGGER.warn("Cannot register function class " + functionType.getName(), e);
            }
        }
    }

    protected abstract Iterable<Class<?>> getFunctionTypes();

    protected abstract Iterable<Class<?>> getProcedureTypes();

}
