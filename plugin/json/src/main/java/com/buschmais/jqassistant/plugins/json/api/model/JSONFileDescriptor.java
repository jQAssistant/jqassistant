package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface JSONFileDescriptor extends JSONDescriptor, FileDescriptor {
    @Relation("CONTAINS")
    JSONArrayDescriptor getArray();

    void setArray(JSONArrayDescriptor array);

    @Relation("CONTAINS")
    JSONObjectDescriptor getObject();

    void setObject(JSONObjectDescriptor object);

    void setParsed(boolean parsable);

    @Property("parsed")
    boolean getParsed();
}
