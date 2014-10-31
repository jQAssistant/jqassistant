package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;

public interface OnColumnDescriptor extends Descriptor {

    int getIndexOrdinalPosition();

    void setIndexOrdinalPosition(int indexOrdinalPosition);

    String getSortSequence();

    void setSortSequence(String name);
}
