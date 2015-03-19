package com.buschmais.jqassistant.core.store.impl;

import org.neo4j.kernel.impl.locking.Locks;
import org.neo4j.kernel.impl.locking.NoOpClient;
import org.neo4j.kernel.impl.locking.community.CommunityLockManger;

/**
 * A lock factory implementation which disables locking (as jQAssistant runs
 * single threaded).
 */
public class NoOpLockFactory extends Locks.Factory {

    public static final String KEY = "NoOp";

    /**
     * Constructor.
     */
    public NoOpLockFactory() {
        super(KEY);
    }

    @Override
    public Locks newInstance(Locks.ResourceType[] resourceTypes) {
        return new CommunityLockManger() {
            @Override
            public Client newClient() {
                return new NoOpClient();
            }
        };
    }
}
