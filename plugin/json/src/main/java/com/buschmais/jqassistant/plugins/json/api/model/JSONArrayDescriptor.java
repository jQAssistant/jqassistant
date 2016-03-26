package com.buschmais.jqassistant.plugins.json.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Relation;

//@Label("Array")
public interface JSONArrayDescriptor extends JSONContainer /*, JSONValueDescriptor<JSONArrayDescriptor>*/
{
    @Relation("CONTAINS_VALUE")
    List<JSONValueDescriptor<?>> getValues();
}
