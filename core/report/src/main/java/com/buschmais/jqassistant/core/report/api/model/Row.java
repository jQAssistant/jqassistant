package com.buschmais.jqassistant.core.report.api.model;

import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.rule.api.model.Hidden;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Row {

    @NonNull
    private final String key;

    @NonNull
    private final Map<String, Column<?>> columns;

    @NonNull
    private final Result.Status status;

    @Builder.Default
    private Optional<Hidden> hidden = Optional.empty();

    public boolean isHidden() {
        if (hidden.isEmpty()) {
            return false;
        }
        return hidden.filter(value -> (!value.getSuppressions()
                .isEmpty() || value.getBaseline()
                .isPresent()))
            .isPresent();
    }
}
