package com.buschmais.jqassistant.core.shared.option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides shared functionality for working with options, e.g. from the command
 * line.
 */
public final class OptionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionHelper.class);

    /**
     * Determine a value from given options.
     *
     * @param defaultValue
     *            The default (i.e. fallback) value.
     * @param overrides
     *            The option that override the default value, the first non-null value will be accepted.
     * @param <T>
     *            The value type.
     * @return The value.
     */
    public static <T> T selectValue(T defaultValue, T... overrides) {
        for (T override : overrides) {
            if (override != null) {
                return override;
            }
        }
        return defaultValue;
    }

    /**
     * Verify if a deprecated option has been used and emit a warning.
     *
     * @param deprecatedOption
     *            The name of the deprecated option.
     * @param value
     *            The provided value.
     * @param option
     *            The option to use.
     * @param <T>
     *            The value type.
     */
    public static <T> void verifyDeprecatedOption(String deprecatedOption, T value, String option) {
        if (value != null) {
            LOGGER.warn("The option '" + deprecatedOption + "' is deprecated, use '" + option + "' instead.");
        }
    }

}
