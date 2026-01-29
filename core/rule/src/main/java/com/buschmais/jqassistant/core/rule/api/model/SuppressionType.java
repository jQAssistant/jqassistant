package com.buschmais.jqassistant.core.rule.api.model;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class SuppressionType {

    @Builder.Default
    private boolean suppressedByBaseline = false;

    @Builder.Default
    private boolean suppressedBySuppression = false;

    private LocalDate suppressUntil;
    private String suppressReason;
}