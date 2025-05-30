package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregationVerificationStrategy extends AbstractMinMaxVerificationStrategy<AggregationVerification> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationVerificationStrategy.class);

    @Override
    public Class<AggregationVerification> getVerificationType() {
        return AggregationVerification.class;
    }

    @Override
    public <T extends ExecutableRule> VerificationResult verify(T executable, AggregationVerification verification, List<String> columnNames, List<Row> rows)
        throws RuleException {
        LOGGER.debug("Verifying result of {}", executable);
        if (rows.isEmpty()) {
            return getStatus(executable, 0, verification.getMin(), verification.getMax());
        }
        if (columnNames.isEmpty()) {
            throw new RuleException("Result contains no columns, at least one with a numeric value is expected.");
        }
        String columnName = verification.getColumn();
        if (columnName == null) {
            columnName = columnNames.get(0);
            LOGGER.debug("No aggregation column specified, using {}", columnName);
        }
        int aggregatedValue = 0;
        for (Row row : rows) {
            Column<?> column = row.getColumns()
                .get(columnName);
            if (column == null) {
                throw new RuleException("The result does not contain a column '" + columnName);
            }
            Object value = column.getValue();
            if (!Number.class.isAssignableFrom(value.getClass())) {
                throw new RuleException("The value in column '" + columnName + "' must be a numeric value but was '" + value + "'");
            }
            aggregatedValue = aggregatedValue + ((Number) value).intValue();
        }
        return getStatus(executable, aggregatedValue, verification.getMin(), verification.getMax());
    }
}
