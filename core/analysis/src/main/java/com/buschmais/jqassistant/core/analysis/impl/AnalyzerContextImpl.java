package com.buschmais.jqassistant.core.analysis.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineManager;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.model.*;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.store.api.Store;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static java.util.Optional.empty;
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

    private final Severity.Threshold warnOnSeverity;

    private final Severity.Threshold failOnSeverity;

    AnalyzerContextImpl(Analyze configuration, ClassLoader classLoader, Store store, BaselineManager baselineManager) throws RuleException {
        this.classLoader = classLoader;
        this.store = store;
        this.baselineManager = baselineManager;
        this.warnOnSeverity = Severity.Threshold.from(configuration.report()
            .warnOnSeverity());
        this.failOnSeverity = Severity.Threshold.from(configuration.report()
            .failOnSeverity());
        this.verificationStrategies = of(new RowCountVerificationStrategy(), new AggregationVerificationStrategy()).collect(
            toMap(VerificationStrategy::getVerificationType, strategy -> strategy));
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
    public Row toRow(ExecutableRule<?> rule, Map<String, Column<?>> columns, Optional<String> primaryColumn) {
        if (rule.getReport() != null) {
            Hidden hidden = Hidden.builder()
                .build();
            String rowKey = ReportHelper.getRowKey(rule, columns);
            if (baselineManager.isExisting(rule, rowKey, columns)) {
                hidden.setBaseline(Optional.of(Hidden.Baseline.builder()
                    .build()));
            }
            for (Map.Entry<String, Column<?>> entry : columns.entrySet()) {
                String columnName = entry.getKey();
                Column<?> column = entry.getValue();
                Object columnValue = column.getValue();
                if (columnValue != null && Suppress.class.isAssignableFrom(columnValue.getClass())) {
                    Suppress suppress = (Suppress) columnValue;
                    String suppressColumn = suppress.getSuppressColumn();
                    if ((suppressColumn != null && suppressColumn.equals(columnName)) || (primaryColumn.isPresent() && primaryColumn.get()
                        .equals(columnName))) {
                        String[] suppressIds = suppress.getSuppressIds();
                        if (validateSuppressUntilDate(suppress.getSuppressUntil())) {
                            for (String suppressId : suppressIds) {
                                if (rule.getId()
                                    .equals(suppressId)) {
                                    Hidden.Suppression suppression = Hidden.Suppression.builder()
                                        .build();
                                    if (StringUtils.isNotEmpty(suppress.getSuppressReason())) {
                                        suppression.setSuppressReason(suppress.getSuppressReason());
                                    }
                                    if (suppress.getSuppressUntil() != null) {
                                        suppression.setSuppressUntil(suppress.getSuppressUntil());
                                    }
                                    hidden.setSuppression(Optional.of(suppression));
                                }
                            }
                        }
                    }
                }
            }
            if (hidden.getSuppression()
                .isPresent() || hidden.getBaseline()
                .isPresent()) {
                return ReportHelper.toRow(rule, columns, Optional.of(hidden));
            }
        }
        return ReportHelper.toRow(rule, columns, empty());
    }

    public boolean validateSuppressUntilDate(LocalDate until) {
        if (until == null) {
            return true;
        } else {
            LocalDate today = LocalDate.now();
            return until.isAfter(today);
        }
    }

    @Override
    public <T extends ExecutableRule<?>> VerificationResult verify(T executable, List<String> columnNames, List<Row> rows) throws RuleException {
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
        List<Row> filteredRows = rows.stream()
            .filter(row -> !row.isHidden())
            .collect(Collectors.toList());
        return strategy.verify(executable, verification, columnNames, filteredRows);
    }

    @Override
    public Result.Status getStatus(VerificationResult verificationResult, Severity severity) {
        if (!verificationResult.isSuccess()) {
            if (severity.exceeds(failOnSeverity)) {
                return FAILURE;
            } else {
                if (severity.exceeds(warnOnSeverity)) {
                    return WARNING;
                }
            }
        }
        return SUCCESS;
    }

    /**
     * Determine the primary column for a rule, i.e. the colum used by tools like
     * SonarQube to attach issues.
     *
     * @param rule
     *     The {@link ExecutableRule}.
     * @param columnNames
     *     The column names returned by the executed rule.
     * @return The name of the primary column.
     */
    @Override
    public Optional<String> getPrimaryColumn(ExecutableRule<?> rule, List<String> columnNames) {
        if (columnNames == null || columnNames.isEmpty()) {
            return empty();
        }
        String primaryColumn = rule.getReport()
            .getPrimaryColumn();
        String firstColumn = columnNames.get(0);
        if (primaryColumn == null) {
            // primary column not explicitly specifed by the rule, so take the first column by default.
            return Optional.of(firstColumn);
        }
        if (!columnNames.contains(primaryColumn)) {
            log.warn("Rule '{}' defines primary column '{}' which is not provided by the result (available columns: {}). Falling back to '{}'.", rule,
                primaryColumn, columnNames, firstColumn);
            primaryColumn = firstColumn;
        }
        return Optional.of(primaryColumn);
    }

}
