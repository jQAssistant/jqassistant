package com.buschmais.jqassistant.core.rule.api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
public class Hidden {

    @Singular
    private List<Suppression> suppressions;

    @Builder.Default
    private Optional<Baseline> baseline = Optional.empty();

    @Getter
    @Builder
    public static class Suppression {
        private LocalDate until;
        private String reason;
    }

    @Getter
    @Builder
    public static class Baseline {
    }
}
