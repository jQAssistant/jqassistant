package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;

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
    public <T extends ExecutableRule> VerificationResult verify(T executable, RowCountVerification verification, List<String> columnNames, List<Row> rows) {
        int rowCount = rows.size();
        for (Row row : rows) {
            if (row.isSuppressed()) {
                rowCount--;
            }
        }
        return getStatus(executable, rowCount, verification.getMin(), verification.getMax());
    }
}
