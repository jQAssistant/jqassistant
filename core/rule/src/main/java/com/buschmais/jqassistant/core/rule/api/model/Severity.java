package com.buschmais.jqassistant.core.rule.api.model;

import java.util.EnumSet;
import java.util.Optional;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static lombok.AccessLevel.PRIVATE;

/**
 * Represents level of rule violations.
 *
 * @author Aparna Chaudhary
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum Severity {

    BLOCKER("blocker", 0),
    CRITICAL("critical", 1),
    MAJOR("major", 2),
    MINOR("minor", 3),
    INFO("info", 4);

    /**
     * Represents a threshold that can be matched against severities to evaluate required actions, e.g. reporting warning/failures or breaking the build.
     */
    @Getter
    @Builder
    @RequiredArgsConstructor(access = PRIVATE)
    public static class Threshold {

        public static final String NEVER = "never";

        private final Optional<Severity> threshold;

        /**
         * Creates a threshold from a {@link Severity}.
         *
         * @param value
         *     The {@link Severity}.
         * @return The {@link Threshold}.
         */
        public static Severity.Threshold from(Severity value) {
            return new Threshold(of(value));
        }

        /**
         * Creates a threshold from a {@link String}.
         * <p>
         * Beside the defined {@link Severity} values a specific value {@link #NEVER} is accepted.
         *
         * @param value
         *     The {@link String}.
         * @return The {@link Threshold}.
         * @throws IllegalArgumentException
         *     If the value cannot be mapped to {@link #NEVER} of an enum value defined {@link Severity}.
         */
        public static Severity.Threshold from(String value) throws RuleException {
            String trimmedValue = value.trim();
            if (NEVER.equalsIgnoreCase(trimmedValue)) {
                return new Threshold(empty());
            }
            return new Threshold(of(Severity.fromValue(trimmedValue)));
        }

        @Override
        public String toString() {
            return threshold.map(severity -> severity.name())
                .orElse(NEVER);
        }
    }

    private final String value;
    private final Integer level;


    /**
     * Return a string representing of the effective severity of a rule.
     * <p>
     * If the severity differs from the given severity then "... (from ...)" both will be returned.
     *
     * @param fromSeverity
     *     The from severity to use, i.e. the default severity of the rule.
     * @return The string representation.
     */
    public String getInfo(Severity fromSeverity) {
        StringBuilder result = new StringBuilder(name());
        if (!this.equals(fromSeverity)) {
            result.append(" (from ")
                .append(fromSeverity)
                .append(")");
        }
        return result.toString();
    }

    /**
     * Evaluates if this {@link Severity} exceeds a given {@link Threshold}.
     *
     * @param threshold
     *     The threshold {@link Severity}.
     * @return <code>true</code> if the threshold is exceeded.
     */
    public boolean exceeds(Threshold threshold) {
        return threshold.getThreshold()
            .map(thresholdSeverity -> this.level <= thresholdSeverity.level)
            .orElse(false);
    }

    /**
     * Retrieves severity based on string representation.
     *
     * @param value
     *     string representation; {@code null} if no matching severity
     *     found.
     * @return {@link Severity}
     */
    public static Severity fromValue(String value) throws RuleException {
        if (value == null) {
            return null;
        }
        for (Severity severity : EnumSet.allOf(Severity.class)) {
            if (severity.value.equals(value.toLowerCase())) {
                return severity;
            }
        }
        throw new RuleException("Unknown severity '" + value + "'");
    }
}
