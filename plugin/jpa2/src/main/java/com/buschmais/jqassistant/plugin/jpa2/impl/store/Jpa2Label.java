package com.buschmais.jqassistant.plugin.jpa2.impl.store;

import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;

/**
 * Defines the JPA labels.
 */
public enum Jpa2Label implements IndexedLabel {

    JPA(false),
    PERSISTENCE(false),
    PERSISTENCEUNIT(false);

    private boolean index;

    /**
     * Constructor.
     *
     * @param index <code>true</code> if nodes with this label shall be indexed.
     */
    private Jpa2Label(boolean index) {
        this.index = index;
    }

    @Override
    public boolean isIndexed() {
        return false;
    }
}
