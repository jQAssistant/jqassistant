package com.buschmais.jqassistant.report;

import com.buschmais.jqassistant.core.analysis.api.ExecutionListener;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.plugin.junit4.impl.schema.Error;
import com.buschmais.jqassistant.plugin.junit4.impl.schema.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ExecutionListener} implementation to write JUnit style reports.
 * <p>Each group is rendered as a test suite to a separate file.</p>
 */
public class JUnitReportWriter implements ExecutionListener {

    private File directory;
    private JAXBContext jaxbContext;

    private Group group;
    private long executableBeginTimestamp;
    private long groupBeginTimestamp;
    private Map<Result, Long> results = new LinkedHashMap<>();

    public JUnitReportWriter(File directory) throws ExecutionListenerException {
        this.directory = directory;
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new ExecutionListenerException("Cannot create jaxb context instance.", e);
        }
    }

    @Override
    public void begin() throws ExecutionListenerException {
    }

    @Override
    public void end() throws ExecutionListenerException {
    }

    @Override
    public void beginGroup(Group group) throws ExecutionListenerException {
        this.group = group;
        this.groupBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void endGroup() throws ExecutionListenerException {
        // TestSuite
        Testsuite testsuite = new Testsuite();
        int tests = 0;
        int failures = 0;
        int errors = 0;
        for (Map.Entry<Result, Long> entry : results.entrySet()) {
            // TestCase
            Result result = entry.getKey();
            long time = entry.getValue().longValue();
            Testcase testcase = new Testcase();
            AbstractExecutable executable = result.getExecutable();
            testcase.setName(executable.getId());
            testcase.setClassname(group.getId());
            testcase.setTime(Long.toString(time));
            List<Map<String, Object>> rows = result.getRows();
            if (executable instanceof Concept && rows.isEmpty()) {
                Failure failure = new Failure();
                failure.setMessage(executable.getDescription());
                failure.setContent("The concept returned an empty result.");
                testcase.getFailure().add(failure);
                failures++;
            } else if (executable instanceof Constraint && !rows.isEmpty()) {
                Error error = new Error();
                error.setMessage(executable.getDescription());
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
            throw new ExecutionListenerException("Cannot write JUNIT4 report.", e);
        }
        this.group = null;
        this.results.clear();
    }

    @Override
    public void beginConcept(Concept concept) throws ExecutionListenerException {
        this.executableBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void endConcept() throws ExecutionListenerException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ExecutionListenerException {
        this.executableBeginTimestamp = System.currentTimeMillis();
    }

    @Override
    public void endConstraint() throws ExecutionListenerException {
    }

    @Override
    public void setResult(Result<? extends AbstractExecutable> result) throws ExecutionListenerException {
        long executableEndTimestamp = System.currentTimeMillis();
        long time = executableEndTimestamp - executableBeginTimestamp;
        this.results.put(result, Long.valueOf(time));
    }
}
