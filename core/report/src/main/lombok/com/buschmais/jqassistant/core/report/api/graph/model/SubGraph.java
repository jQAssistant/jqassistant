package com.buschmais.jqassistant.core.report.api.graph.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class SubGraph extends Identifiable {

    private Node parent = null;

    private Map<Long, Node> nodes = new HashMap<>();

    private Map<Long, Relationship> relationships = new HashMap<>();

    private Map<Long, SubGraph> subGraphs = new HashMap<>();
}
