package com.buschmais.jqassistant.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;

import com.buschmais.jqassistant.model.ClassDescriptor;

public abstract class AbstractInVMGraphStore implements GraphStore {

    private final Map<ClassDescriptor, Node> classNodes = new HashMap<ClassDescriptor, Node>();

    private GraphDatabaseService database;

    private WrappingNeoServerBootstrapper server;

    @Override
    public void start() {
        database = startDatabase();
        server = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) database);
        server.start();
    }

    @Override
    public void stop() {
        server.stop();
        stopDatabase(database);
    }

    @Override
    public void createClassNodesWithDependencies(Map<ClassDescriptor, Set<ClassDescriptor>> dependencies) {
        Transaction transaction = database.beginTx();
        try {
            for (Map.Entry<ClassDescriptor, Set<ClassDescriptor>> entry : dependencies.entrySet()) {
                Node dependentNode = getClassNode(entry.getKey());
                for (ClassDescriptor dependency : entry.getValue()) {
                    Node dependencyNode = getClassNode(dependency);
                    dependentNode.createRelationshipTo(dependencyNode, JQAssistantRelationType.DEPENDS_ON);
                }
            }
            transaction.success();
        } finally {
            transaction.finish();
        }

    }

    private Node getClassNode(ClassDescriptor classDescriptor) {
        Node classNode = classNodes.get(classDescriptor);
        if (classNode == null) {
            classNode = database.createNode();
            String fqcn = getFullQualifiedClassName(classDescriptor);
            classNode.setProperty("name", fqcn);
            classNodes.put(classDescriptor, classNode);
            database.index().forNodes("classes").add(classNode, "name", fqcn);
        }
        return classNode;
    }

    protected String getFullQualifiedClassName(ClassDescriptor classDescriptor) {
        return classDescriptor.getPackageDescriptor().getName() + "." + classDescriptor.getName();
    }

    protected abstract GraphDatabaseService startDatabase();

    protected abstract void stopDatabase(GraphDatabaseService database);

}