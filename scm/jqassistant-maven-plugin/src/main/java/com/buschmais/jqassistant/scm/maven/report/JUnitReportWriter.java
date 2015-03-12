package com.buschmais.jqassistant.scm.maven.report;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.plugin.junit.impl.schema.*;
import com.buschmais.jqassistant.plugin.junit.impl.schema.Error;

/**
 * {@link AnalysisListener} implementation to write JUnit style reports.
 * <p>
 * Each group is rendered as a test suite to a separate file.
 * </p>
 */
public class JUnitReportWriter implements AnalysisListener<AnalysisListenerException> {

    private File directory;
    private JAXBContext jaxbContext;

    private Group group;
    private long ruleBeginTimestamp;
    private long groupBeginTimestamp;
    private Map<Result<? extends ExecutableRule>, Long> results = new LinkedHashMap<>();

    public JUnitReportWriter(File directory) throws AnalysisListenerException {
        this.directory = directory;
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new AnalysisListenerException("Cannot create jaxb context instance.", e);
        }
    }

    @Override
    public void begin() throws AnalysisListenerException {
    }

    @Override
    public void end() throws AnalysisListenerException {
    }

    @Override
    public void beginGroup(Group group) throws AnalysisListenerException {
        this.group = group;
        this.groupBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void endGroup() throws AnalysisListenerException {
        // TestSuite
        Testsuite testsuite = new Testsuite();
        int tests = 0;
        int failures = 0;
        int errors = 0;
        for (Map.Entry<Result<? extends ExecutableRule>, Long> entry : results.entrySet()) {
            // TestCase
            Result<? extends Rule> result = entry.getKey();
            long time = entry.getValue().longValue();
            Testcase testcase = new Testcase();
            Rule rule = result.getRule();
            testcase.setName(rule.getId());
            testcase.setClassname(group.getId());
            testcase.setTime(Long.toString(time));
            List<Map<String, Object>> rows = result.getRows();
            if (rule instanceof Concept && rows.isEmpty()) {
                Failure failure = new Failure();
                failure.setMessage(rule.getDescription());
                failure.setContent("The concept returned an empty result.");
                testcase.getFailure().add(failure);
                failures++;
            } else if (rule instanceof Constraint && !rows.isEmpty()) {
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
        testsuite.setTests(Integer.toString(tests));
        testsuite.setFailures(Integer.toString(failures));
        testsuite.setErrors(Integer.toString(errors));
        testsuite.setName(group.getId());
        long groupTime = System.currentTimeMillis() - groupBeginTimestamp;
        testsuite.setTime(Long.toString(groupTime));
        // TestSuite
        File file = new File(directory, "TEST-" + group.getId() + ".xml");
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(testsuite, file);
        } catch (JAXBException e) {
            throw new AnalysisListenerException("Cannot write JUnit report.", e);
        }
        this.group = null;
        this.results.clear();
    }

    @Override
    public void beginConcept(Concept concept) throws AnalysisListenerException {
        this.ruleBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void endConcept() throws AnalysisListenerException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws AnalysisListenerException {
        this.ruleBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void endConstraint() throws AnalysisListenerException {
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws AnalysisListenerException {
        long ruleEndTimestamp = System.currentTimeMillis();
        long time = ruleEndTimestamp - ruleBeginTimestamp;
        this.results.put(result, Long.valueOf(time));
    }
}
