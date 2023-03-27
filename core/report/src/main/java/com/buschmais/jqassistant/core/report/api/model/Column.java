package com.buschmais.jqassistant.core.report.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Column<V> {

    private V value;

    private String label;

}
