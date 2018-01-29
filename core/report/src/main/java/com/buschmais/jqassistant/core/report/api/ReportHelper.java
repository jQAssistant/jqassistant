package com.buschmais.jqassistant.core.report.api;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.xo.api.CompositeObject;

import org.slf4j.Logger;

/**
 * Provides utility functionality for creating reports.
 */
public final class ReportHelper {


    private interface LoggingStrategy {

        void log(String message);

    }

    public static String CONSTRAINT_VIOLATION_HEADER = "--[ Constraint Violation ]-----------------------------------------";

    public static String CONCEPT_FAILED_HEADER = "--[ Concept Application Failure ]----------------------------------";

    private static String FOOTER = "-------------------------------------------------------------------";

    private final LoggingStrategy warnLogger;

    private final LoggingStrategy errorLogger;

    private final LoggingStrategy debugLogger;

    /**
     * Constructor.
     *
     * @param log
     *            The logger to use for logging messages.
     */
    public ReportHelper(final Logger log) {
        this.errorLogger = new LoggingStrategy() {
            @Override
            public void log(String message) {
                log.error(message);
            }
        };
        this.warnLogger = new LoggingStrategy() {
            @Override
            public void log(String message) {
                log.warn(message);
            }
        };
        this.debugLogger = new LoggingStrategy() {
            @Override
            public void log(String message) {
                log.debug(message);
            }
        };
    }

    /**
     * Verifies the concept results returned by the {@link InMemoryReportWriter}
     * .
     *
     * @param warnOnSeverity
     *            The severity threshold to warn.
     * @param failOnSeverity
     *            The severity threshold to fail.
     * @param inMemoryReportWriter
     *            The {@link InMemoryReportWriter}
     * @return The number of failed concepts, i.e. for breaking the build if
     *         higher than 0.
     */
    public int verifyConceptResults(Severity warnOnSeverity, Severity failOnSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults().values();
        return verifyRuleResults(conceptResults, warnOnSeverity, failOnSeverity, "Concept", CONCEPT_FAILED_HEADER, false);
    }

    /**
     * Verifies the constraint results returned by the
     * {@link InMemoryReportWriter} .
     *
     * @param warnOnSeverity
     *            The severity threshold to warn.
     * @param failOnSeverity
     *            The severity threshold to fail.
     * @param inMemoryReportWriter
     *            The {@link InMemoryReportWriter}
     * @return The number of failed concepts, i.e. for breaking the build if
     *         higher than 0.
     */
    public int verifyConstraintResults(Severity warnOnSeverity, Severity failOnSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Constraint>> constraintResults = inMemoryReportWriter.getConstraintResults().values();
        return verifyRuleResults(constraintResults, warnOnSeverity, failOnSeverity, "Constraint", CONSTRAINT_VIOLATION_HEADER, true);
    }

    /**
     * Verifies the given results and logs messages.
     *
     * @param results
     *            The collection of results to verify.
     * @param warnOnSeverity
     *            The severity threshold to warn.
     * @param failOnSeverity
     *            The severity threshold to fail.
     * @param type
     *            The type of the rules (as string).
     * @param header
     *            The header to use.
     * @param logResult
     *            if `true` log the result of the executable rule.
     * @return The number of detected violations.
     */
    private int verifyRuleResults(Collection<? extends Result<? extends ExecutableRule>> results, Severity warnOnSeverity, Severity failOnSeverity, String type,
            String header, boolean logResult) {
        int violations = 0;
        for (Result<?> result : results) {
            if (Result.Status.FAILURE.equals(result.getStatus())) {
                ExecutableRule rule = result.getRule();
                String severityInfo = rule.getSeverity().getInfo(result.getSeverity());
                List<String> resultRows = getResultRows(result, logResult);
                // violation severity level check
                boolean warn = warnOnSeverity != null && result.getSeverity().getLevel() <= warnOnSeverity.getLevel();
                boolean fail = failOnSeverity != null && result.getSeverity().getLevel() <= failOnSeverity.getLevel();
                LoggingStrategy loggingStrategy;
                if (fail) {
                    violations++;
                    loggingStrategy = errorLogger;
                } else if (warn) {
                    loggingStrategy = warnLogger;
                } else {
                    loggingStrategy = debugLogger;
                }
                log(loggingStrategy, rule, resultRows, severityInfo, type, header);
            }
        }
        return violations;
    }

    private void log(LoggingStrategy loggingStrategy, ExecutableRule rule, List<String> resultRows, String severityInfo, String type, String header) {
        loggingStrategy.log(header);
        loggingStrategy.log(type + ": " + rule.getId());
        loggingStrategy.log("Severity: " + severityInfo);
        loggingStrategy.log("Number of violations: " + resultRows.size());
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
     *            The result.
     * @param logResult
     *            if `false` suppress logging the result.
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
     *            The rule.
     */
    private void logDescription(LoggingStrategy loggingStrategy, Rule rule) {
        String description = rule.getDescription();
        StringTokenizer tokenizer = new StringTokenizer(description, "\n");
        while (tokenizer.hasMoreTokens()) {
            loggingStrategy.log(tokenizer.nextToken().replaceAll("(\\r|\\n|\\t)", ""));
        }
    }

    /**
     * Converts a value to its string representation.
     *
     * @param value
     *            The value.
     * @return The string representation
     */
    public static String getLabel(Object value) {
        if (value != null) {
            if (value instanceof CompositeObject) {
                CompositeObject descriptor = (CompositeObject) value;
                String label = getLanguageLabel(descriptor);
                return label != null ? label : descriptor.toString();
            } else if (value.getClass().isArray()) {
                Object[] objects = (Object[]) value;
                return getLabel(Arrays.asList(objects));
            } else if (value instanceof Iterable) {
                StringBuilder sb = new StringBuilder();
                for (Object o : ((Iterable) value)) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(getLabel(o));
                }
                return "[" + sb.toString() + "]";
            } else if (value instanceof Map) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(entry.getKey());
                    sb.append(":");
                    sb.append(getLabel(entry.getValue()));
                }
                return "{" + sb.toString() + "}";
            }
            return value.toString();
        }
        return null;
    }

    private static String getLanguageLabel(CompositeObject descriptor) {
        LanguageElement elementValue = LanguageHelper.getLanguageElement(descriptor);
        if (elementValue != null) {
            SourceProvider sourceProvider = elementValue.getSourceProvider();
            return sourceProvider.getName(descriptor);
        }
        return null;
    }

}
