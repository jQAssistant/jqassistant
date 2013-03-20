package com.buschmais.jqassistant.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.tooling.GlobalGraphOperations;

import com.buschmais.jqassistant.store.model.ClassDescriptor;
import com.buschmais.jqassistant.store.model.PackageDescriptor;

public abstract class AbstractInVMGraphStore implements GraphStore {

    public interface TransactionalOperation<T> {
        public T run();
    }

    public class TransactionFailedException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -415937972419446380L;

        public TransactionFailedException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private final Map<ClassDescriptor, Node> classNodes = new HashMap<ClassDescriptor, Node>();
    private final Map<PackageDescriptor, Node> packageNodes = new HashMap<PackageDescriptor, Node>();

    private GraphDatabaseService database;

    private WrappingNeoServerBootstrapper server;

    @Override
    public void start() {
        database = startDatabase();
        try {
            runTransactional(new TransactionalOperation<Void>() {

                @Override
                public Void run() {
                    for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
                        for (Relationship relationShip : node.getRelationships()) {
                            relationShip.delete();
                        }
                    }
                    return null;
                }

            });
        } catch (TransactionFailedException e) {
            throw new IllegalStateException("Cannot clean database.", e);
        }
        server = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) database);
        server.start();
    }

    @Override
    public void stop() {
        server.stop();
        stopDatabase(database);
    }

    @Override
    public void createClassNodesWithDependencies(final Map<ClassDescriptor, Set<ClassDescriptor>> dependencies) {
        try {
            runTransactional(new TransactionalOperation<Void>() {
                @Override
                public Void run() {
                    for (Map.Entry<ClassDescriptor, Set<ClassDescriptor>> entry : dependencies.entrySet()) {
                        Node dependentNode = getClassNode(entry.getKey());
                        for (ClassDescriptor dependency : entry.getValue()) {
                            Node dependencyNode = getClassNode(dependency);
                            dependentNode.createRelationshipTo(dependencyNode, JQAssistantRelationType.DEPENDS_ON);
                        }
                    }
                    return null;
                }
            });
        } catch (TransactionFailedException e) {
            throw new IllegalStateException("Cannot create class nodes.", e);
        }
    }

    protected abstract GraphDatabaseService startDatabase();

    protected abstract void stopDatabase(GraphDatabaseService database);

    protected <T> T runTransactional(TransactionalOperation<T> txOperation) throws TransactionFailedException {
        Transaction transaction = database.beginTx();
        try {
            T result = txOperation.run();
            transaction.success();
            return result;
        } catch (Exception e) {
            transaction.failure();
            throw new TransactionFailedException("Cannot complete transaction.", e);
        } finally {
            transaction.finish();
        }

    }

    private Node getClassNode(ClassDescriptor classDescriptor) {
        Node classNode = classNodes.get(classDescriptor);
        if (classNode == null) {
            classNode = database.createNode();
            String fqcn = classDescriptor.getFullQualifiedName();
            classNode.setProperty("name", fqcn);
            classNodes.put(classDescriptor, classNode);
            database.index().forNodes("classes").add(classNode, "name", fqcn);
            Node packageNode = getPackageNode(classDescriptor.getPackageDescriptor());
            packageNode.createRelationshipTo(classNode, JQAssistantRelationType.CONTAINS);
        }
        return classNode;
    }

    private Node getPackageNode(PackageDescriptor packageDescriptor) {
        Node packageNode = packageNodes.get(packageDescriptor);
        if (packageNode == null) {
            packageNode = database.createNode();
            String fqcn = packageDescriptor.getFullQualifiedName();
            packageNode.setProperty("name", fqcn);
            packageNodes.put(packageDescriptor, packageNode);
            database.index().forNodes("packages").add(packageNode, "name", fqcn);
            PackageDescriptor parentPackageDescriptor = packageDescriptor.getParent();
            if (parentPackageDescriptor != null) {
                Node parentPackageNode = getPackageNode(parentPackageDescriptor);
                parentPackageNode.createRelationshipTo(packageNode, JQAssistantRelationType.CONTAINS);
            }
        }
        return packageNode;
    }

}