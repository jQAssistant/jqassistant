package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

public class RowCountVerificationStrategy extends AbstractMinMaxVerificationStrategy<RowCountVerification> {

    public RowCountVerificationStrategy(Report configuration) {
        super(configuration);
    }

    @Override
    public Class<RowCountVerification> getVerificationType() {
        return RowCountVerification.class;
    }

    @Override
    public <T extends ExecutableRule> Result.Status verify(T executable, Severity severity, RowCountVerification verification, List<String> columnNames,
        List<Row> rows) {
        return getStatus(executable, severity, rows.size(), verification.getMin(), verification.getMax());
    }

}
