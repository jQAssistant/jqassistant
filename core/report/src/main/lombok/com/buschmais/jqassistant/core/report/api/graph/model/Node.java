package com.buschmais.jqassistant.core.report.api.graph.model;

import java.util.Set;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Node extends PropertyContainer {

    private Set<String> labels = new TreeSet<>();

}
