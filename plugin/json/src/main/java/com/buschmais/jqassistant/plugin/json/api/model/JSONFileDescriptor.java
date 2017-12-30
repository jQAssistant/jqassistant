package com.buschmais.jqassistant.plugin.json.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents a file containing an JSON document.
 */
public interface JSONFileDescriptor
        extends JSONDescriptor, FileDescriptor, ValidDescriptor {
    @Relation("CONTAINS")
    JSONArrayDescriptor getArray();

    void setArray(JSONArrayDescriptor array);

    @Relation("CONTAINS")
    JSONObjectDescriptor getObject();

    void setObject(JSONObjectDescriptor object);

    @Relation("CONTAINS")
    JSONScalarValueDescriptor getScalarValue();

    void setScalarValue(JSONScalarValueDescriptor value);
}
