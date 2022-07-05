package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.*;

import lombok.RequiredArgsConstructor;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class AbstractMinMaxVerificationStrategy<T extends Verification> implements VerificationStrategy<T> {

    private final Report configuration;

    protected <T extends ExecutableRule> Result.Status getStatus(T executable, Severity severity, int value, Integer min, Integer max) {
        if (min == null && max == null) {
            if (executable instanceof Concept) {
                min = 1;
            }
            if (executable instanceof Constraint) {
                max = 0;
            }
        }
        boolean success = (min == null || value >= min) && (max == null || value <= max);
        return getStatus(severity, success);
    }

    private Result.Status getStatus(Severity severity, boolean success) {
        if (!success) {
            Severity.Threshold failOnSeverity = configuration.failOnSeverity();
            Severity.Threshold warnOnSeverity = configuration.warnOnSeverity();
            if (severity.exceeds(failOnSeverity)) {
                return FAILURE;
            } else {
                if (severity.exceeds(warnOnSeverity)) {
                    return WARNING;
                }
            }
        }
        return SUCCESS;
    }

}
