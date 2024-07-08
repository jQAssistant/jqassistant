package com.buschmais.jqassistant.core.report.api.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class Row {

    @NonNull
    private final String key;

    @NonNull
    private final Map<String, Column<?>> columns;

}
