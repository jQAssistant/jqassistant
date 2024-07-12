package com.buschmais.jqassistant.core.report.api.model;

import java.util.Map;

import lombok.*;

@Builder
@Getter
@ToString
@RequiredArgsConstructor
public class Row {

    @NonNull
    private final String key;

    @NonNull
    private final Map<String, Column<?>> columns;

}
