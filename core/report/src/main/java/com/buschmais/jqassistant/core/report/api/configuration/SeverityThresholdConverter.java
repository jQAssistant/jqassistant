package com.buschmais.jqassistant.core.report.api.configuration;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import org.eclipse.microprofile.config.spi.Converter;

/**
 * A {@link Converter} implementation for {@link com.buschmais.jqassistant.core.rule.api.model.Severity.Threshold}s.
 */
public class SeverityThresholdConverter implements Converter<Severity.Threshold> {

    @Override
    public Severity.Threshold convert(String value) {
        try {
            return Severity.Threshold.from(value);
        } catch (RuleException e) {
            throw new IllegalArgumentException("Cannot convert " + value + " to a severity threshold.");
        }
    }

}
