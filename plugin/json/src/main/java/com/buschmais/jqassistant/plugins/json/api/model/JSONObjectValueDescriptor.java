package com.buschmais.jqassistant.plugins.json.api.model;

public interface JSONObjectValueDescriptor extends JSONValueDescriptor<JSONObjectDescriptor>, JSONObjectDescriptor {

    @Override
    void setValue(JSONObjectDescriptor value);

    @Override
    JSONObjectDescriptor getValue();
}
