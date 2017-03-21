package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;

public class AggregationVerificationStrategy extends AbstractMinMaxVerificationStrategy implements VerificationStrategy<AggregationVerification> {

    @Override
    public Class<AggregationVerification> getVerificationType() {
        return AggregationVerification.class;
    }

    @Override
    public <T extends ExecutableRule> Result.Status verify(T executable, AggregationVerification verification, List<String> columnNames,
            List<Map<String, Object>> rows) throws RuleExecutorException {
        String column = verification.getColumn();
        if (column == null) {
            column = columnNames.get(0);
        }
        Integer min = verification.getMin();
        Integer max = verification.getMax();
        for (Map<String, Object> row : rows) {
            Object value = row.get(column);
            if (value == null || !Number.class.isAssignableFrom(value.getClass())) {
                throw new RuleExecutorException("The value in column '" + column + "' must be a non-null numeric value but was '" + value + "'");
            }
            int aggregationValue = ((Number) value).intValue();
            Result.Status status = getStatus(executable, aggregationValue, min, max);
            if (Result.Status.FAILURE.equals(status)) {
                return Result.Status.FAILURE;
            }
        }
        return Result.Status.SUCCESS;
    }
}
