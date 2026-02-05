package com.buschmais.jqassistant.core.report.api.model;

import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.rule.api.model.Hidden;

import lombok.*;

@Builder
@Getter
@ToString
public class Row {

    @NonNull
    private final String key;

    @NonNull
    private final Map<String, Column<?>> columns;

    private Optional<Hidden> hidden;

    public boolean isSuppressed() {
        return hidden.filter(value -> (value.getSuppression()
                        .isPresent() || value.getBaseline()
                        .isPresent()))
                .isPresent();
    }
}
