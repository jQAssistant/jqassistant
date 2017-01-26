package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines an abstract rule which is has an unique identifier and references a
 * query.
 */
public abstract class AbstractExecutableRule extends AbstractSeverityRule implements ExecutableRule {

    /**
     * The executable.
     */
    private Executable executable;

    /**
     * The required parameters.
     */
    private Map<String, Parameter> parameters = new HashMap<>();

    /**
     * The rules which must be applied before this rule can be executed. The value determines if the rule is optional.
     */
    private Map<String, Boolean> requiresConcepts = new HashMap<>();

    /**
     * Describes the verification of the result of an executable rule.
     */
    private Verification verification;

    /**
     * Describes report settings.
     */
    private Report report;

    @Override
    public Map<String, Boolean> getRequiresConcepts() {
        return requiresConcepts;
    }

    @Override
    public Executable getExecutable() {
        return executable;
    }

    @Override
    public Map<String, Parameter> getParameters() {
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

    protected abstract static class Builder<B extends Builder<B, R>, R extends AbstractExecutableRule> extends AbstractSeverityRule.Builder<B, R> {

        protected Builder(R rule) {
            super(rule);
        }

        public B requiresConceptIds(Map<String, Boolean> requiresConcepts) {
            AbstractExecutableRule r = get();
            r.requiresConcepts.putAll(requiresConcepts);
            return builder();
        }

        public B executable(Executable executable) {
            AbstractExecutableRule r = get();
            r.executable = executable;
            return builder();
        }

        public B parameters(Map<String, Parameter> parameters) {
            AbstractExecutableRule r = get();
            r.parameters.putAll(parameters);
            return builder();
        }

        public B verification(Verification verification) {
            AbstractExecutableRule r = get();
            r.verification = verification;
            return builder();
        }

        public B report(Report report) {
            AbstractExecutableRule r = get();
            r.report = report;
            return builder();
        }
    }
}
