package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.VerificationStrategy;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RowCountVerification;

public class RowCountVerificationStrategy implements VerificationStrategy<RowCountVerification> {

    @Override
    public Class<RowCountVerification> getVerificationType() {
        return RowCountVerification.class;
    }

    @Override
    public <T extends ExecutableRule> Result.Status verify(T executable, RowCountVerification verification, List<String> columnNames,
            List<Map<String, Object>> rows) throws AnalysisException {
        Result.Status status;
        if (executable instanceof Concept) {
            status = rows.size() > 0 ? Result.Status.SUCCESS : Result.Status.FAILURE;
        } else if (executable instanceof Constraint) {
            status = rows.size() == 0 ? Result.Status.SUCCESS : Result.Status.FAILURE;
        } else {
            throw new AnalysisException("Unsupported rule " + executable);
        }
        return status;
    }

}
