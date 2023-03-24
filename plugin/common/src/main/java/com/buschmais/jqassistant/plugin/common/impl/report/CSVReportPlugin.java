package com.buschmais.jqassistant.plugin.common.impl.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import com.opencsv.CSVWriter;

/**
 * A {@link ReportPlugin} that writes a rule result as CSV file.
 */
public class CSVReportPlugin implements ReportPlugin {

    public static final String REPORT_TYPE = "csv";

    public static final String PROPERTY_SEPARATOR = "csv.report.separator";

    public static final String PROPERTY_QUOTE_CHAR = "csv.report.quoteChar";

    public static final String PROPERTY_ESCAPE_CHAR = "csv.report.escapeChar";

    public static final String PROPERTY_LINE_END = "csv.report.lineEnd";

    private ReportContext reportContext;

    private char separator;

    private char quotechar;

    private char escapechar;

    private String lineEnd;

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
        this.reportContext = reportContext;
        this.separator = getChar(properties, PROPERTY_SEPARATOR, CSVWriter.DEFAULT_SEPARATOR);
        this.quotechar = getChar(properties, PROPERTY_QUOTE_CHAR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
        this.escapechar = getChar(properties, PROPERTY_ESCAPE_CHAR, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
        Object lineEndProperty = properties.get(PROPERTY_LINE_END);
        this.lineEnd = lineEndProperty != null ? lineEndProperty.toString() : CSVWriter.DEFAULT_LINE_END;
    }

    private char getChar(Map<String, Object> properties, String key, char defaultValue) throws ReportException {
        String value = (String) properties.get(key);
        if (value == null) {
            return defaultValue;
        } else if (value.length() == 1) {
            return value.charAt(0);
        }
        throw new ReportException("Expecting a single character for property " + key + ", got " + value);
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        String fileName = ReportHelper.escapeRuleId(result.getRule()) + ".csv";
        File reportDirectory = reportContext.getReportDirectory(REPORT_TYPE);
        File csvFile = new File(reportDirectory, fileName);
        try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(csvFile)), separator, quotechar, escapechar, lineEnd)) {
            List<String> columnNames = result.getColumnNames();
            if (columnNames != null) {
                csvWriter.writeNext(columnNames.toArray(new String[columnNames.size()]));
                for (Row row : result.getRows()) {
                    List<String> csvRow = new ArrayList<>(columnNames.size());
                    for (String columnName : columnNames) {
                        csvRow.add(row.getColumns().get(columnName).getLabel());
                    }
                    csvWriter.writeNext(csvRow.toArray(new String[columnNames.size()]));
                }
            }
        } catch (IOException e) {
            throw new ReportException("Cannot write CSV report " + csvFile, e);
        }
        try {
            reportContext.addReport("CSV", result.getRule(), ReportContext.ReportType.LINK, csvFile.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new ReportException("Cannot convert file " + csvFile.getPath() + " to URL.", e);
        }
    }
}
