package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineManager;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.Suppress;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.model.Verification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.store.api.Store;

import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

/**
 * Implementation of the {@link AnalyzerContext}.
 */
@Slf4j
class AnalyzerContextImpl implements AnalyzerContext {

    private static final Verification DEFAULT_VERIFICATION = RowCountVerification.builder()
        .build();

    private final ClassLoader classLoader;

    private final Store store;

    private final BaselineManager baselineManager;

    private final Map<Class<? extends Verification>, VerificationStrategy<?>> verificationStrategies;

    AnalyzerContextImpl(Analyze configuration, ClassLoader classLoader, Store store, BaselineManager baselineManager) throws RuleException {
        this.classLoader = classLoader;
        this.store = store;
        this.baselineManager = baselineManager;
        this.verificationStrategies = of(new RowCountVerificationStrategy(configuration.report()),
            new AggregationVerificationStrategy(configuration.report())).collect(toMap(VerificationStrategy::getVerificationType, strategy -> strategy));
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public <T> Column<T> toColumn(T value) {
        return ReportHelper.toColumn(value);
    }

    @Override
    public Row toRow(ExecutableRule<?> rule, Map<String, Column<?>> columns) {
        return ReportHelper.toRow(rule, columns);
    }

    @Override
    public <T extends ExecutableRule<?>> boolean isSuppressed(T executableRule, String primaryColumn, Row row) {
        if (baselineManager.isExisting(executableRule, row)) {
            return true;
        }
        String ruleId = executableRule.getId();
        Map<String, Column<?>> columns = row.getColumns();
        for (Map.Entry<String, Column<?>> entry : columns.entrySet()) {
            String columnName = entry.getKey();
            Column<?> column = entry.getValue();
            Object columnValue = column.getValue();
            if (columnValue != null && Suppress.class.isAssignableFrom(columnValue.getClass())) {
                Suppress suppress = (Suppress) columnValue;
                String suppressColumn = suppress.getSuppressColumn();
                if ((suppressColumn != null && suppressColumn.equals(columnName)) || primaryColumn.equals(columnName)) {
                    String[] suppressIds = suppress.getSuppressIds();
                    for (String suppressId : suppressIds) {
                        if (ruleId.equals(suppressId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public <T extends ExecutableRule<?>> Result.Status verify(T executable, Severity severity, List<String> columnNames, List<Row> rows) throws RuleException {
        Verification verification = executable.getVerification();
        if (verification == null) {
            log.debug("Using default verification for '{}'.", executable);
            verification = DEFAULT_VERIFICATION;
        }
        VerificationStrategy strategy = verificationStrategies.get(verification.getClass());
        if (strategy == null) {
            throw new RuleException("Result verification not supported: " + verification.getClass()
                .getName());
        }
        return strategy.verify(executable, severity, verification, columnNames, rows);
    }
}
