package com.buschmais.jqassistant.core.report.api.graph.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class PropertyContainer extends Identifiable {

    private Map<String,Object> properties = new HashMap<>();

}
