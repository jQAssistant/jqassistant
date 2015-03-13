package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.VerificationStrategy;
import com.buschmais.jqassistant.core.analysis.api.rule.AggregationVerification;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

public class AggregationVerificationStrategy implements VerificationStrategy<AggregationVerification> {

    @Override
    public Class<AggregationVerification> getVerificationType() {
        return AggregationVerification.class;
    }

    @Override
    public <T extends ExecutableRule> Result.Status verify(T executable, AggregationVerification verification, List<String> columnNames,
            List<Map<String, Object>> rows) throws AnalysisException {
        String column = verification.getColumn();
        if (column == null) {
            column = columnNames.get(0);
        }
        for (Map<String, Object> row : rows) {
            Object value = row.get(column);
            if (value == null || !Number.class.isAssignableFrom(value.getClass())) {
                throw new AnalysisException("The value in column '" + column + "' must be a non-null numeric value but was '" + value + "'");
            }
            int aggregationValue = ((Number) value).intValue();
            if (executable instanceof Concept) {
                if (aggregationValue == 0) {
                    return Result.Status.FAILURE;
                }
            } else if (executable instanceof Constraint) {
                if (aggregationValue > 0) {
                    return Result.Status.FAILURE;
                }
            }
        }
        return Result.Status.SUCCESS;
    }
}
