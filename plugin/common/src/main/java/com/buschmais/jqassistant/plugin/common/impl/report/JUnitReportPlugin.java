package com.buschmais.jqassistant.plugin.common.impl.report;

import java.io.File;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.AbstractReportPlugin;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.plugin.junit.impl.schema.*;
import com.buschmais.jqassistant.plugin.junit.impl.schema.Error;

/**
 * {@link ReportPlugin} implementation to write JUnit style reports.
 *
 * Each group is rendered as a test suite to a separate file.
 *
 */
public class JUnitReportPlugin extends AbstractReportPlugin {

    // Properties
    public static final String JUNIT_REPORT_DIRECTORY = "junit.report.directory";

    // Default values
    public static final String DEFAULT_JUNIT_REPORT_DIRECTORY = "junit";

    private JAXBContext jaxbContext;

    private File reportDirectory;

    private Deque<Group> groups = new LinkedList<>();
    private long ruleBeginTimestamp;
    private long groupBeginTimestamp;
    private Map<Group, Map<Result<? extends ExecutableRule>, Long>> results = new HashMap<>();

    @Override
    public void initialize() throws ReportException {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new ReportException("Cannot create jaxb context instance.", e);
        }
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        String junitReportDirectory = (String) properties.get(JUNIT_REPORT_DIRECTORY);
        this.reportDirectory = junitReportDirectory != null ? new File(junitReportDirectory) : reportContext.getReportDirectory(DEFAULT_JUNIT_REPORT_DIRECTORY);
    }

    @Override
    public void beginGroup(Group group) {
        this.groups.push(group);
        this.results.put(group, new LinkedHashMap<Result<? extends ExecutableRule>, Long>());
        this.groupBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void endGroup() throws ReportException {
        Group group = groups.pop();
        Map<Result<? extends ExecutableRule>, Long> groupResults = this.results.remove(group);
        // TestSuite
        Testsuite testsuite = new Testsuite();
        int tests = 0;
        int failures = 0;
        int errors = 0;
        String id = "jQAssistant-" + group.getId().replaceAll("\\:", "_");
        for (Map.Entry<Result<? extends ExecutableRule>, Long> entry : groupResults.entrySet()) {
            // TestCase
            Result<? extends Rule> result = entry.getKey();
            long time = entry.getValue().longValue();
            Testcase testcase = new Testcase();
            Rule rule = result.getRule();
            testcase.setName(rule.getId());
            testcase.setClassname(id);
            testcase.setTime(Long.toString(time));
            List<Map<String, Object>> rows = result.getRows();
            if (Result.Status.FAILURE.equals(result.getStatus())) {
                if (rule instanceof Concept) {
                    Failure failure = new Failure();
                    failure.setMessage(rule.getDescription());
                    failure.setContent("The concept could not be applied");
                    testcase.getFailure().add(failure);
                    failures++;
                } else if (rule instanceof Constraint) {
                    Error error = new Error();
                    error.setMessage(rule.getDescription());
                    StringBuilder sb = new StringBuilder();
                    for (Map<String, Object> row : rows) {
                        for (Map.Entry<String, Object> rowEntry : row.entrySet()) {
                            sb.append(rowEntry.getKey());
                            sb.append("=");
                            sb.append(rowEntry.getValue());
                        }
                    }
                    error.setContent(sb.toString());
                    testcase.getError().add(error);
                    errors++;
                }
                tests++;
                testsuite.getTestcase().add(testcase);
            }
        }
        testsuite.setTests(Integer.toString(tests));
        testsuite.setFailures(Integer.toString(failures));
        testsuite.setErrors(Integer.toString(errors));
        testsuite.setName(id);
        long groupTime = System.currentTimeMillis() - groupBeginTimestamp;
        testsuite.setTime(Long.toString(groupTime));
        // TestSuite
        File file = new File(reportDirectory, "TEST-" + id + ".xml");
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(testsuite, file);
        } catch (JAXBException e) {
            throw new ReportException("Cannot write JUnit report.", e);
        }
    }

    @Override
    public void beginConcept(Concept concept) {
        this.ruleBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void beginConstraint(Constraint constraint) {
        this.ruleBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) {
        long ruleEndTimestamp = System.currentTimeMillis();
        long time = ruleEndTimestamp - ruleBeginTimestamp;
        Group group = groups.peek();
        Map<Result<? extends ExecutableRule>, Long> groupResults = results.get(group);
        groupResults.put(result, Long.valueOf(time));
    }
}
