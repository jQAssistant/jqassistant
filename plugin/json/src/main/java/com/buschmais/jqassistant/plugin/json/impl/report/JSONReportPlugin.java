package com.buschmais.jqassistant.plugin.json.impl.report;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;

/**
 * Reports rule results as JSON files
 */
public class JSONReportPlugin implements ReportPlugin {

    private static final String REPORT_TYPE = "json";

    private ObjectMapper objectMapper;

    private ReportContext reportContext;

    @Override
    public void initialize() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        this.reportContext = reportContext;
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        List<String> columnNames = result.getColumnNames();
        String fileName = ReportHelper.escapeRuleId(result.getRule()) + ".json";
        File reportDirectory = reportContext.getReportDirectory(REPORT_TYPE);
        File file = new File(reportDirectory, fileName);
        // Do not include columns as top-level object if result contains only one column
        Optional<String> columnName = columnNames.size() == 1 ? of(columnNames.get(0)) : empty();
        Stream<?> stream = result.getRows()
            .stream()
            .map(row -> getRow(row.getColumns(), columnName));
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, stream.iterator());
        } catch (IOException e) {
            throw new ReportException("Cannot write JSON report.", e);
        }
    }

    /**
     * Extracts the value(s) of a single row. If a columName is given only the value of that column is used, otherwise all column values are returned as {@link Map}.
     *
     * @param columns
     *     The columns.
     * @param columName
     *     The column name.
     * @return The value(s).
     */
    private static Object getRow(Map<String, Column<?>> columns, Optional<String> columName) {
        return columName.<Object>map(s -> columns.get(s)
                .getValue())
            .orElseGet(() -> columns.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, k -> k.getValue()
                    .getValue())));
    }
}
