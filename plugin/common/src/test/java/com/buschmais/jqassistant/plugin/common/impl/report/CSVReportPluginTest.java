package com.buschmais.jqassistant.plugin.common.impl.report;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class CSVReportPluginTest extends AbstractReportPluginTest {

    private Concept conceptWithRows = Concept.builder().id("test:ConceptWithRows").description("testConceptWithRows").severity(Severity.MINOR).build();

    private Concept conceptWithoutRows = Concept.builder().id("test:ConceptWithoutRows").description("testConceptWithoutRows").severity(Severity.MINOR).build();

    public CSVReportPluginTest() {
        super(new CSVReportPlugin());
    }

    @Test
    public void conceptWithRows() throws ReportException, IOException {
        Map<String, Object> properties = Collections.emptyMap();
        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(conceptWithRows, SUCCESS);
        plugin.end();

        File csvReportDirectory = reportContext.getReportDirectory("csv");
        assertThat(csvReportDirectory.exists(), equalTo(true));

        File reportFile = new File(csvReportDirectory, "test_ConceptWithRows.csv");
        assertThat(reportFile.exists(), equalTo(true));

        List<ReportContext.Report<?>> reports = reportContext.getReports(conceptWithRows);
        assertThat(reports.size(), equalTo(1));
        ReportContext.Report<?> report = reports.get(0);
        assertThat(report.getRule(), is(conceptWithRows));
        assertThat(report.getLabel(), equalTo("CSV"));
        assertThat(report.getReportType(), equalTo(ReportContext.ReportType.LINK));
        assertThat(report.getUrl(), equalTo(reportFile.toURI().toURL()));

        String content = FileUtils.readFileToString(reportFile);
        assertThat(content, equalTo("\"String\",\"Double\",\"Named\",\"EscapedString\"\n" + "\"foo\",\"42.0\",\"Test\",\"\"\"'\"\n"));
    }

    @Test
    public void separatorAndEscapeChar() throws ReportException, IOException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CSVReportPlugin.PROPERTY_SEPARATOR, ";");
        properties.put(CSVReportPlugin.PROPERTY_QUOTE_CHAR, "'");
        properties.put(CSVReportPlugin.PROPERTY_ESCAPE_CHAR, "'");
        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(conceptWithRows, SUCCESS);
        plugin.end();

        File csvReportDirectory = reportContext.getReportDirectory("csv");
        assertThat(csvReportDirectory.exists(), equalTo(true));

        File report = new File(csvReportDirectory, "test_ConceptWithRows.csv");
        assertThat(report.exists(), equalTo(true));

        String content = FileUtils.readFileToString(report);
        assertThat(content, equalTo("'String';'Double';'Named';'EscapedString'\n" + "'foo';'42.0';'Test';'\"'''\n"));
    }

    @Test
    public void conceptWithoutRows() throws ReportException, IOException {
        Map<String, Object> properties = Collections.emptyMap();
        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(conceptWithoutRows, SUCCESS);
        plugin.end();

        File csvReportDirectory = reportContext.getReportDirectory("csv");
        assertThat(csvReportDirectory.exists(), equalTo(true));

        File report = new File(csvReportDirectory, "test_ConceptWithoutRows.csv");
        assertThat(report.exists(), equalTo(true));

        String content = FileUtils.readFileToString(report);
        assertThat(content, equalTo(""));
    }

    @Override
    protected <T extends ExecutableRule<?>> Result<T> getResult(T rule, Result.Status status) {
        if (rule.equals(conceptWithRows)) {
            HashMap<String, Object> row = new HashMap<>();
            row.put("String", "foo");
            row.put("Double", 42.0);
            row.put("Named", new NamedDescriptor() {
                @Override
                public <I> I getId() {
                    return null;
                }

                @Override
                public <T> T as(Class<T> type) {
                    return null;
                }

                @Override
                public <D> D getDelegate() {
                    return null;
                }

                @Override
                public String getName() {
                    return "Test";
                }

                @Override
                public void setName(String name) {
                }
            });
            row.put("EscapedString", "\"'");
            return Result.<T> builder().rule(rule).severity(rule.getSeverity()).status(status).columnNames(asList("String", "Double", "Named", "EscapedString"))
                    .rows(asList(row)).build();
        } else {
            return Result.<T> builder().rule(rule).severity(rule.getSeverity()).status(status).columnNames(null).rows(null).build();
        }
    }

}
