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
     * Determine the first non-null value from the given options.
     *
     * @param values
     *            The option that override the default value, the first non-null
     *            value will be accepted.
     * @param <T>
     *            The value type.
     * @return The value.
     */
    public static <T> T coalesce(T... values) {
        for (T override : values) {
            if (override != null) {
                return override;
            }
        }
        return null;
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
