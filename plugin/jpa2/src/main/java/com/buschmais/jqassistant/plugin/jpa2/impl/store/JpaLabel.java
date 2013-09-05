package com.buschmais.jqassistant.plugin.jpa2.impl.store;

import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;

/**
 * Defines the JPA labels.
 */
public enum JpaLabel implements PrimaryLabel {

    PERSISTENCEUNIT(false);

    private boolean index;

    /**
     * Constructor.
     *
     * @param index <code>true</code> if nodes with this label shall be indexed.
     */
    private JpaLabel(boolean index) {
        this.index = index;
    }

    @Override
    public boolean isIndexed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
