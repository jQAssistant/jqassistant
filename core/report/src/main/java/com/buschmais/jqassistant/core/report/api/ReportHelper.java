package com.buschmais.jqassistant.core.report.api;

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.slf4j.Logger;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Provides utility functionality for creating reports.
 */
public final class ReportHelper {

    private static String CONSTRAINT_VIOLATION_HEADER = "--[ Constraint Violation ]-----------------------------------------";

    private static String CONCEPT_FAILED_HEADER = "--[ Concept Application Failure ]----------------------------------";

    private static String FOOTER = "-------------------------------------------------------------------";

    private Logger logger;

    /**
     * Constructor.
     *
     * @param log
     *            The logger to use for logging messages.
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
     * @param inMemoryReportWriter
     *            The
     *            {@link com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter}
     *            .
     */
    public int verifyConceptResults(Severity violationSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults().values();
        return verifyRuleResults(violationSeverity, conceptResults, "Concept", CONCEPT_FAILED_HEADER, false);
    }

    /**
     * Verifies the constraint violations returned by the
     * {@link InMemoryReportWriter}. Returns the count of constraints having
     * severity higher than the provided severity level.
     *
     * @param violationSeverity
     *            severity level to use for verification
     * @param inMemoryReportWriter
     *            The {@link InMemoryReportWriter}.
     */
    public int verifyConstraintResults(Severity violationSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Constraint>> constraintResults = inMemoryReportWriter.getConstraintResults().values();
        return verifyRuleResults(violationSeverity, constraintResults, "Constraint", CONSTRAINT_VIOLATION_HEADER, true);
    }

    private int verifyRuleResults(Severity violationSeverity, Collection<? extends Result<? extends ExecutableRule>> results, String type, String header,
            boolean showResult) {
        int violations = 0;
        for (Result<?> result : results) {
            if (Result.Status.FAILURE.equals(result.getStatus())) {
                ExecutableRule rule = result.getRule();

                logger.error(header);
                logger.error(type + ": " + rule.getId());
                logger.error("Severity: " + rule.getSeverity().getInfo(result.getSeverity()));

                logDescription(rule);

                if (showResult) {
                    for (Map<String, Object> columns : result.getRows()) {
                        StringBuilder message = new StringBuilder();
                        for (Map.Entry<String, Object> entry : columns.entrySet()) {
                            if (message.length() > 0) {
                                message.append(", ");
                            }
                            message.append(entry.getKey());
                            message.append('=');
                            String stringValue = getStringValue(entry.getValue());
                            message.append(stringValue);
                        }
                        logger.error("  " + message.toString());
                    }
                }

                logger.error(FOOTER);
                logger.error(System.lineSeparator());

                // violationSeverity level check
                if (result.getSeverity().getLevel() <= violationSeverity.getLevel()) {
                    violations++;
                }
            }
        }
        return violations;
    }

    /**
     * Log the description of a rule.
     * 
     * @param rule
     *            The rule.
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
     * @param value
     *            The value.
     * @return The string representation
     */
    public static String getStringValue(Object value) {
        if (value != null) {
            if (value instanceof Descriptor) {
                Descriptor descriptor = (Descriptor) value;
                LanguageElement elementValue = LanguageHelper.getLanguageElement(descriptor);
                if (elementValue != null) {
                    SourceProvider sourceProvider = elementValue.getSourceProvider();
                    return sourceProvider.getName(descriptor);
                }
            } else if (value instanceof Iterable) {
                StringBuilder sb = new StringBuilder();
                for (Object o : ((Iterable) value)) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(getStringValue(o));
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
                    sb.append(getStringValue(entry.getValue()));
                }
                return "{" + sb.toString() + "}";
            }
            return value.toString();
        }
        return null;
    }
}
