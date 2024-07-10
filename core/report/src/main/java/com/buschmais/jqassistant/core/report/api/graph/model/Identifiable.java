package com.buschmais.jqassistant.core.report.api.graph.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public abstract class Identifiable {

    private long id;

    private String label;

}
