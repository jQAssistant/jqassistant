package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.*;

import lombok.RequiredArgsConstructor;

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
        return (min == null || value >= min) && (max == null || value <= max) ? Result.Status.SUCCESS : Result.Status.FAILURE;
    }

}
