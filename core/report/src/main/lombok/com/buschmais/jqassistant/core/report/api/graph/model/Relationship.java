package com.buschmais.jqassistant.core.report.api.graph.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Relationship extends PropertyContainer {

    private String type;

    private Node startNode;

    private Node endNode;
    
}
