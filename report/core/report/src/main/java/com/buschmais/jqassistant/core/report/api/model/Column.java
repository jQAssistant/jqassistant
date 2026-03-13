package com.buschmais.jqassistant.core.report.api.model;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.source.SourceLocation;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Column<V> {

    private V value;

    private String label;

    private Optional<SourceLocation<?>> sourceLocation;

}
