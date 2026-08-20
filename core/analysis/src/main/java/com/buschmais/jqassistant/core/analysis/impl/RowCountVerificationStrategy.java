package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

public class RowCountVerificationStrategy extends AbstractMinMaxVerificationStrategy<RowCountVerification> {

    @Override
    public Class<RowCountVerification> getVerificationType() {
        return RowCountVerification.class;
    }

    @Override
    public <T extends ExecutableRule> VerificationResult verifyColumns(T executable, RowCountVerification verification, List<String> columnNames,
        Map<String, Column<?>> columns) {
        return getStatus(executable, 1, 0, verification.getMin(), verification.getMax());
    }

    @Override
    public <T extends ExecutableRule> VerificationResult verifyRows(T executable, RowCountVerification verification, List<String> columnNames, List<Row> rows) {
        int rowCount = 0;
        int hiddenRowCount = 0;
        for (Row row : rows) {
            if (row.isHidden()) {
                hiddenRowCount++;
            } else {
                rowCount++;
            }
        }
        return getStatus(executable, rowCount, hiddenRowCount, verification.getMin(), verification.getMax());
    }
}
