package com.buschmais.jqassistant.core.report.api;

import java.util.*;
import java.util.stream.StreamSupport;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.neo4j.api.model.Neo4jPropertyContainer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Provides utility functionality for creating reports.
 */
public final class ReportHelper {

    public interface FailAction<E extends Exception> {

        void fail(String message) throws E;

    }

    private interface LoggingStrategy {

        void log(String message);

    }

    public static String CONSTRAINT_VIOLATION_HEADER = "--[ Constraint Violation ]-----------------------------------------";
    public static String CONCEPT_FAILED_HEADER = "--[ Concept Application Failure ]----------------------------------";
    private static String FOOTER = "-------------------------------------------------------------------";

    private final Report configuration;

    private final LoggingStrategy infoLogger;
    private final LoggingStrategy warnLogger;
    private final LoggingStrategy errorLogger;
    private final LoggingStrategy debugLogger;

    /**
     * Constructor.
     *
     * @param log
     *     The {@link Logger} to use for reporting.
     */
    public ReportHelper(Report configuration, Logger log) {
        this.configuration = configuration;
        this.infoLogger = message -> log.info(message);
        this.errorLogger = message -> log.error(message);
        this.warnLogger = message -> log.warn(message);
        this.debugLogger = message -> log.debug(message);
    }

    /**
     * Escape the id of the given rule in a way such that it can be used as file
     * name.
     *
     * @param rule
     *     The rule.
     * @return The escaped name.
     */
    public static String escapeRuleId(Rule rule) {
        return rule != null ?
            rule.getId()
                .replaceAll("\\:", "_") :
            null;
    }

    /**
     * Converts a value to its string representation.
     *
     * @param value
     *     The value.
     * @return The string representation
     */
    public static String getLabel(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof CompositeObject) {
            CompositeObject descriptor = (CompositeObject) value;
            String label = getLanguageLabel(descriptor);
            if (label != null) {
                return label;
            }
            return getLabel(descriptor.getDelegate());
        } else if (value.getClass()
            .isArray()) {
            Object[] objects = (Object[]) value;
            return getLabel(asList(objects));
        } else if (value instanceof Iterable) {
            Spliterator<?> spliterator = ((Iterable<?>) value).spliterator();
            List<String> elements = StreamSupport.stream(spliterator, false)
                .map(element -> getLabel(element))
                .collect(toList());
            return StringUtils.join(elements, ", ");
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.size() == 1) {
                return getLabel(map.values()
                    .iterator()
                    .next());
            } else {
                List<String> entries = map.entrySet()
                    .stream()
                    .map(entry -> getLabel(entry.getKey() + ":" + getLabel(entry.getValue())))
                    .collect(toList());
                return getLabel(entries);
            }
        } else if (value instanceof Neo4jPropertyContainer) {
            Neo4jPropertyContainer neo4jPropertyContainer = (Neo4jPropertyContainer) value;
            return getLabel(neo4jPropertyContainer.getProperties());
        }
        return value.toString();
    }

    private static String getLanguageLabel(CompositeObject descriptor) {
        LanguageElement elementValue = LanguageHelper.getLanguageElement(descriptor);
        if (elementValue != null) {
            SourceProvider sourceProvider = elementValue.getSourceProvider();
            return sourceProvider.getName(descriptor);
        }
        return null;
    }

    /**
     * Verify the result provided by the {@link InMemoryReportPlugin}.
     * <p>
     * This method logs detected warnings and failures and determines if the build shall break. The latter might be implemented by throwing an exception in the provided {@link FailAction} or by evaluation the return value.
     *
     * @param inMemoryReport
     *     The {@link InMemoryReportPlugin}.
     * @param failAction
     *     The {@link FailAction} that shall be triggered for breaking a build.
     * @return <code>true</code> If the build shall break according to the detected failures and the setting {@link Report#breakOnFailure()}.
     * @throws E
     *     The exception declared by the {@link FailAction}.
     */
    public <E extends Exception> boolean verify(InMemoryReportPlugin inMemoryReport, FailAction<E> failAction) throws E {
        infoLogger.log("Verifying results (warn-on-severity=" + configuration.warnOnSeverity() + ", fail-on-severity=" + configuration.failOnSeverity()
            + ", break-on-failure=" + configuration.breakOnFailure() + ")");
        int conceptFailures = verifyConceptResults(inMemoryReport);
        int constraintFailures = verifyConstraintResults(inMemoryReport);
        int totalFailures = conceptFailures + constraintFailures;
        if (totalFailures > 0 && this.configuration.breakOnFailure()) {
            failAction.fail("Failed rules detected: " + conceptFailures + " concepts, " + constraintFailures + " constraints");
            return true;
        }
        return false;
    }

    /**
     * Verifies the concept results returned by the {@link InMemoryReportPlugin} .
     *
     * @param inMemoryReportWriter
     *     The {@link InMemoryReportPlugin}
     * @return The number of failed concepts, i.e. for breaking the build if higher
     * than 0.
     */
    int verifyConceptResults(InMemoryReportPlugin inMemoryReportWriter) {
        Collection<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults()
            .values();
        return verifyRuleResults(conceptResults, "Concept", CONCEPT_FAILED_HEADER, false);
    }

    /**
     * Verifies the constraint results returned by the {@link InMemoryReportPlugin}
     * .
     *
     * @param inMemoryReportWriter
     *     The {@link InMemoryReportPlugin}
     * @return The number of failed concepts, i.e. for breaking the build if higher
     * than 0.
     */
    int verifyConstraintResults(InMemoryReportPlugin inMemoryReportWriter) {
        Collection<Result<Constraint>> constraintResults = inMemoryReportWriter.getConstraintResults()
            .values();
        return verifyRuleResults(constraintResults, "Constraint", CONSTRAINT_VIOLATION_HEADER, true);
    }

    /**
     * Verifies the given results and logs messages.
     *
     * @param results
     *     The collection of results to verify.
     * @param type
     *     The type of the rules (as string).
     * @param header
     *     The header to use.
     * @param logResult
     *     if `true` log the result of the executable rule.
     * @return The number of detected failures.
     */
    private int verifyRuleResults(Collection<? extends Result<? extends ExecutableRule>> results, String type, String header, boolean logResult) {
        int failures = 0;
        for (Result<?> result : results) {
            Result.Status status = result.getStatus();
            ExecutableRule rule = result.getRule();
            Severity resultSeverity = result.getSeverity();
            String severityInfo = resultSeverity.getInfo(rule.getSeverity());
            List<String> resultRows = getResultRows(result, logResult);
            // violation severity level check
            LoggingStrategy loggingStrategy;
            switch (status) {
            case WARNING:
                loggingStrategy = warnLogger;
                break;
            case FAILURE:
                failures++;
                loggingStrategy = errorLogger;
                break;
            default:
                loggingStrategy = debugLogger;
                break;
            }
            log(loggingStrategy, rule, resultRows, severityInfo, type, header);
        }
        return failures;
    }

    private void log(LoggingStrategy loggingStrategy, ExecutableRule rule, List<String> resultRows, String severityInfo, String type, String header) {
        loggingStrategy.log(header);
        loggingStrategy.log(type + ": " + rule.getId());
        loggingStrategy.log("Severity: " + severityInfo);
        loggingStrategy.log("Number of rows: " + resultRows.size());
        logDescription(loggingStrategy, rule);
        // we need lambdas...
        for (String row : resultRows) {
            loggingStrategy.log(row);
        }
        loggingStrategy.log(FOOTER);
        loggingStrategy.log(System.lineSeparator());
    }

    /**
     * Convert the result rows into a string representation.
     *
     * @param result
     *     The result.
     * @param logResult
     *     if `false` suppress logging the result.
     * @return The string representation as list.
     */
    private List<String> getResultRows(Result<?> result, boolean logResult) {
        List<String> rows = new ArrayList<>();
        if (logResult) {
            for (Map<String, Object> columns : result.getRows()) {
                StringBuilder row = new StringBuilder();
                for (Map.Entry<String, Object> entry : columns.entrySet()) {
                    if (row.length() > 0) {
                        row.append(", ");
                    }
                    row.append(entry.getKey());
                    row.append('=');
                    String stringValue = getLabel(entry.getValue());
                    row.append(stringValue);
                }
                rows.add("  " + row.toString());
            }
        }
        return rows;
    }

    /**
     * Log the description of a rule.
     *
     * @param rule
     *     The rule.
     */
    private void logDescription(LoggingStrategy loggingStrategy, Rule rule) {
        String description = rule.getDescription();
        StringTokenizer tokenizer = new StringTokenizer(description, "\n");
        while (tokenizer.hasMoreTokens()) {
            loggingStrategy.log(tokenizer.nextToken()
                .replaceAll("(\\r|\\n|\\t)", ""));
        }
    }

}
