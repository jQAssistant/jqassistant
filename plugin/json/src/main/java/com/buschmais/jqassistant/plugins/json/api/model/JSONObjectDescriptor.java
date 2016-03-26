package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Object")
public interface JSONObjectDescriptor extends JSONContainer /*, JSONValueDescriptor<JSONObjectDescriptor>*/
{


    @Relation("HAS_KEY")
    List<JSONKeyDescriptor> getKeys();
}
