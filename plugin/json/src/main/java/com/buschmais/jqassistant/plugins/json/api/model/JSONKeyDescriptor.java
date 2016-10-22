package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Key")
public interface JSONKeyDescriptor extends JSONDescriptor {
    @Relation("HAS_VALUE")
    JSONArrayDescriptor getArray();

    void setArray(JSONArrayDescriptor array);

    @Relation("HAS_VALUE")
    JSONObjectDescriptor getObject();

    void setObject(JSONObjectDescriptor object);

    @Relation("HAS_VALUE")
    JSONScalarValueDescriptor getScalarValue();

    void setScalarValue(JSONScalarValueDescriptor scalar);

    @Property("name")
    String getName();

    void setName(String name);
}
