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

    @Builder.Default
    private Optional<Suppression> suppression = Optional.empty();

    @Builder.Default
    private Optional<Baseline> baseline = Optional.empty();

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