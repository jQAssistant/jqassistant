package com.buschmais.jqassistant.core.report.api;

import java.util.*;

import org.slf4j.Logger;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Provides utility functionality for creating reports.
 */
public final class ReportHelper {

    public static String CONSTRAINT_VIOLATION_HEADER = "--[ Constraint Violation ]-----------------------------------------";

    public static String CONCEPT_FAILED_HEADER = "--[ Concept Application Failure ]----------------------------------";

    private static String FOOTER = "-------------------------------------------------------------------";

    private Logger logger;

    /**
     * Constructor.
     *
     * @param log The logger to use for logging messages.
     */
    public ReportHelper(Logger log) {
        this.logger = log;
    }

    /**
     * Verifies the concept results returned by the
     * {@link com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter} .
     * <p>
     * An error message is logged for each concept which did not return a result
     * (i.e. has not been applied).
     * </p>
     *
     * @param inMemoryReportWriter The
     *                             {@link com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter}
     *                             .
     */
    public int verifyConceptResults(Severity violationSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults().values();
        return verifyRuleResults(conceptResults, violationSeverity, "Concept", CONCEPT_FAILED_HEADER, false);
    }

    /**
     * Verifies the constraint violations returned by the
     * {@link InMemoryReportWriter}. Returns the count of constraints having
     * severity higher than the provided severity level.
     *
     * @param violationSeverity    severity level to use for verification
     * @param inMemoryReportWriter The {@link InMemoryReportWriter}.
     */
    public int verifyConstraintResults(Severity violationSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Constraint>> constraintResults = inMemoryReportWriter.getConstraintResults().values();
        return verifyRuleResults(constraintResults, violationSeverity, "Constraint", CONSTRAINT_VIOLATION_HEADER, true);
    }

    /**
     * Verifies the given results and logs messages.
     *
     * @param results           The collection of results to verify.
     * @param violationSeverity The severity to use for identifying violations (i.e.
     *                          threshold).
     * @param type              The type of the rules (as string).
     * @param header            The header to use.
     * @param logResult         if <code>true</code> log the result of the executable rule.
     * @return The number of detected violations.
     */
    private int verifyRuleResults(Collection<? extends Result<? extends ExecutableRule>> results, Severity violationSeverity, String type, String header,
                                  boolean logResult) {
        int violations = 0;
        for (Result<?> result : results) {
            if (Result.Status.FAILURE.equals(result.getStatus())) {
                ExecutableRule rule = result.getRule();
                String severityInfo = rule.getSeverity().getInfo(result.getSeverity());
                List<String> resultRows = getResultRows(result, logResult);
                // violation severity level check
                if (result.getSeverity().getLevel() <= violationSeverity.getLevel()) {
                    violations++;

                    logger.error(header);
                    logger.error(type + ": " + rule.getId());
                    logger.error("Severity: " + severityInfo);

                    logDescription(rule);

                    // we need lambdas...
                    for (String row : resultRows) {
                        logger.error(row);
                    }

                    logger.error(FOOTER);
                    logger.error(System.lineSeparator());
                } else {
                    logger.warn(type + " failed: " + rule.getId() + ", Severity: " + severityInfo);
                    // we need lambdas...
                    for (String row : resultRows) {
                        logger.debug(row);
                    }
                }
            }
        }
        return violations;
    }

    /**
     * Convert the result rows into a string representation.
     *
     * @param result    The result.
     * @param logResult if <code>false</code> suppress logging the result.
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
     * @param rule The rule.
     */
    private void logDescription(Rule rule) {
        String description = rule.getDescription();
        StringTokenizer tokenizer = new StringTokenizer(description, "\n");
        while (tokenizer.hasMoreTokens()) {
            logger.error(tokenizer.nextToken().replaceAll("(\\r|\\n|\\t)", ""));
        }
    }

    /**
     * Converts a value to its string representation.
     *
     * @param value The value.
     * @return The string representation
     */
    public static String getLabel(Object value) {
        if (value != null) {
            if (value instanceof Descriptor) {
                Descriptor descriptor = (Descriptor) value;
                String label = getLanguageLabel(descriptor);
                return label != null ? label : descriptor.toString();
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

    private static String getLanguageLabel(Descriptor descriptor) {
        LanguageElement elementValue = LanguageHelper.getLanguageElement(descriptor);
        if (elementValue != null) {
            SourceProvider sourceProvider = elementValue.getSourceProvider();
            return sourceProvider.getName(descriptor);
        }
        return null;
    }
}
