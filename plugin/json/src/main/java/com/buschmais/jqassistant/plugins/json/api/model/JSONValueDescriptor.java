package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.xo.api.annotation.Abstract;

@Abstract
public interface JSONValueDescriptor<V> extends JSONDescriptor, ValueDescriptor<V> {
}

