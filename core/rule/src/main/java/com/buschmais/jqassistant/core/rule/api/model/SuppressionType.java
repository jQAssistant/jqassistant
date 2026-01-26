package com.buschmais.jqassistant.core.rule.api.model;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum SuppressionType {

    BASELINE,
    SUPPRESSION;

    @Setter
    private String supressReason;

    @Setter
    private LocalDate supressUntil;

}
