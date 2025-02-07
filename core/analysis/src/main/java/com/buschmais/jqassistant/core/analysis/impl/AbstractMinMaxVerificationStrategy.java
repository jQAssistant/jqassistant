package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Verification;

public abstract class AbstractMinMaxVerificationStrategy<T extends Verification> implements VerificationStrategy<T> {

    protected final <E extends ExecutableRule> VerificationResult getStatus(E executable, int value, Integer min, Integer max) {
        if (min == null && max == null) {
            if (executable instanceof Concept) {
                min = 1;
            }
            if (executable instanceof Constraint) {
                max = 0;
            }
        }
        boolean successful = (min == null || value >= min) && (max == null || value <= max);
        return VerificationResult.builder()
            .success(successful)
            .rowCount(value)
            .build();
    }
}
