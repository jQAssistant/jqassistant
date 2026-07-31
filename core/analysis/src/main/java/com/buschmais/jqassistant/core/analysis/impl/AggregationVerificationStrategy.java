package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

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
    public <T extends ExecutableRule> VerificationResult verifyColumns(T executable, AggregationVerification verification, List<String> columnNames,
        Map<String, Column<?>> columns) throws RuleException {
        String aggregationColumnName = getAggregationColumnName(verification, columnNames);
        Integer aggregationValue = getAggregationValue(columns, aggregationColumnName);
        return getStatus(executable, aggregationValue, verification.getMin(), verification.getMax());
    }

    @Override
    public <T extends ExecutableRule> VerificationResult verifyRows(T executable, AggregationVerification verification, List<String> columnNames,
        List<Row> rows) throws RuleException {
        LOGGER.debug("Verifying result of {}", executable);
        if (rows.isEmpty()) {
            return getStatus(executable, 0, verification.getMin(), verification.getMax());
        }
        String columnName = getAggregationColumnName(verification, columnNames);
        int aggregatedValue = 0;
        for (Row row : rows) {
            Integer value = getAggregationValue(row.getColumns(), columnName);
            aggregatedValue = aggregatedValue + value;
        }
        return getStatus(executable, aggregatedValue, verification.getMin(), verification.getMax());
    }

    private static String getAggregationColumnName(AggregationVerification verification, List<String> columnNames) throws RuleException {
        if (columnNames.isEmpty()) {
            throw new RuleException("Result contains no columns, at least one with a numeric value is expected.");
        }
        String columnName = verification.getColumn();
        if (columnName == null) {
            columnName = columnNames.get(0);
            LOGGER.debug("No aggregation column specified, using {}", columnName);
        }
        return columnName;
    }

    private static Integer getAggregationValue(Map<String, Column<?>> columns, String aggregationColumnName) throws RuleException {
        Column<?> column = columns.get(aggregationColumnName);
        if (column == null) {
            throw new RuleException("The result does not contain a column '" + aggregationColumnName);
        }
        Object value = column.getValue();
        if (!Number.class.isAssignableFrom(value.getClass())) {
            throw new RuleException("The value in column '" + aggregationColumnName + "' must be a numeric value but was '" + value + "'");
        }
        return ((Number) value).intValue();
    }

}
