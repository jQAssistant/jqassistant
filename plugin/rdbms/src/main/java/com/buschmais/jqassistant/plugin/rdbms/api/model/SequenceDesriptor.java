package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Sequence")
public interface SequenceDesriptor extends RdbmsDescriptor, NamedDescriptor {

    long getIncrement();

    void setIncrement(long increment);

    long getMinimumValue();

    void setMinimumValue(long minimumValue);

    long getMaximumValue();

    void setMaximumValue(long maximumValue);

    boolean isCycle();

    void setCycle(boolean cycle);
}
