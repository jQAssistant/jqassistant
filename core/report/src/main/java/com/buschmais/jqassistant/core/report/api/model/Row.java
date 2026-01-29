package com.buschmais.jqassistant.core.report.api.model;

import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.SuppressionType;

import lombok.*;

@Builder
@Getter
@ToString
public class Row {

    @NonNull
    private final String key;

    @NonNull
    private final Map<String, Column<?>> columns;

    private SuppressionType suppressionType;


    public boolean isSuppressed() {
        if (getSuppressionType() != null) {
            return (this.getSuppressionType()
                    .isSuppressedByBaseline() || this.getSuppressionType()
                    .isSuppressedBySuppression());
        }
        return false;
    }

}
