package com.buschmais.jqassistant.core.rule.api.model;

import java.time.LocalDate;
import java.util.Optional;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class Hidden {

    private Optional<Suppression> suppression;

    private Optional<Baseline> baseline;

    @Setter
    @Getter
    @Builder
    public static class Suppression {
        private LocalDate suppressUntil;
        private String suppressReason;
    }

    @Setter
    @Getter
    @Builder
    public static class Baseline {
    }
}