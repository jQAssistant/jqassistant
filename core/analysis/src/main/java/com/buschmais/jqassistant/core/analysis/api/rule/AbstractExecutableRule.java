package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Defines an abstract rule which is has an unique identifier and references a Query.
 */
public abstract class AbstractExecutableRule extends AbstractRule implements ExecutableRule {

    /**
     * The severity of the constraint.
     */
    private Severity severity;

    /**
     * The optional deprecation message.
     */
    private String deprecation;

    /**
     * The executable.
     */
    private Executable executable;

    /**
     * The parameters to use.
     */
    private Map<String, Object> parameters;

    /**
     * The rules which must be applied before this rule can be executed.
     */
    private Set<String> requiresConcepts;

    /**
     * Describes the verification of the result of an executable rule.
     */
    private Verification verification;

    private Report report;

    /**
     * Constructor.
     *
     * @param id
     *            The id.
     * @param description
     *            The human readable description.
     * @param ruleSource
     *            The rule source.
     * @param severity
     *            The severity.
     * @param deprecation
     *            The deprecation message.
     * @param executable
     *            The executable.
     * @param parameters
     *            The parametes.
     * @param requiresConcepts
     *            The required concept ids.
     * @param verification
     *            The result verification.
     * @param report
     *            The report definition.
     */
    protected AbstractExecutableRule(String id, String description, RuleSource ruleSource, Severity severity, String deprecation,
            Executable executable, Map<String, Object> parameters, Set<String> requiresConcepts, Verification verification, Report report) {
        super(id, description, ruleSource);
        this.severity = severity;
        this.deprecation = deprecation;
        this.executable = executable;
        this.parameters = parameters;
        this.requiresConcepts = requiresConcepts;
        this.verification = verification;
        this.report = report;
    }

    /**
     * Returns the severity.
     *
     * @return {@link Severity}
     */
    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String getDeprecation() {
        return deprecation;
    }

    @Override
    public Set<String> getRequiresConcepts() {
        return requiresConcepts;
    }

    @Override
    public Executable getExecutable() {
        return executable;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Verification getVerification() {
        return verification;
    }

    @Override
    public Report getReport() {
        return report;
    }
}
