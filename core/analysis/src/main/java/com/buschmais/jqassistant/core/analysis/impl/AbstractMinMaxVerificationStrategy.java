package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

public abstract class AbstractMinMaxVerificationStrategy {

    protected <T extends ExecutableRule> Result.Status getStatus(T executable, int value, Integer min, Integer max) {
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
