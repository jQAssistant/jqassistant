package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.math.BigInteger;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Sequence")
public interface SequenceDesriptor extends RdbmsDescriptor, NamedDescriptor {

    long getIncrement();

    void setIncrement(long increment);

    BigInteger getMinimumValue();

    void setMinimumValue(BigInteger minimumValue);

    BigInteger getMaximumValue();

    void setMaximumValue(BigInteger maximumValue);

    boolean isCycle();

    void setCycle(boolean cycle);
}
