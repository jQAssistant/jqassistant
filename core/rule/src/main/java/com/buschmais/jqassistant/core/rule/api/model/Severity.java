package com.buschmais.jqassistant.core.rule.api.model;

import java.util.EnumSet;

/**
 * Represents level of rule violations.
 *
 * @author Aparna Chaudhary
 */
public enum Severity {

    BLOCKER("blocker", 0),
    CRITICAL("critical", 1),
    MAJOR("major", 2),
    MINOR("minor", 3),
    INFO("info", 4);

    private final String value;
    private final Integer level;

    /**
     * Constructor
     *
     * @param value
     *     value of severity
     * @param level
     *     violation level
     */
    Severity(String value, Integer level) {
        this.value = value;
        this.level = level;
    }

    /**
     * Returns string representation of severity
     *
     * @return string representation
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns violation level
     *
     * @return violation level
     */
    public Integer getLevel() {
        return level;
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

    /**
     * Returns string representation of severity values.
     *
     * @return {@link Severity}
     */
    public static String[] names() {
        int i = 0;
        String[] names = new String[Severity.values().length];
        for (Severity severity : Severity.values()) {
            names[i++] = severity.value;
        }
        return names;
    }

    /**
     * Return a string representing of the effective severity of a rule.
     * <p>
     * If the severity differs from the
     *
     * @param fromSeverity
     *     The from severity to use, i.e. the default severity of the rule.
     * @return The string representation.
     */
    public String getInfo(Severity fromSeverity) {
        StringBuffer result = new StringBuffer(name());
        if (!this.equals(fromSeverity)) {
            result.append(" (from ")
                .append(fromSeverity)
                .append(")");
        }
        return result.toString();
    }

}
