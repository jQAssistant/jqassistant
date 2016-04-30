package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Abstract base class for rules with a severity.
 */
public abstract class AbstractSeverityRule extends AbstractRule implements SeverityRule {
    /**
     * The severity of the rule.
     */
    private Severity severity;

    @Override
    public Severity getSeverity() {
        return severity;
    }

    protected abstract static class Builder<B extends Builder<B, R>, R extends AbstractSeverityRule> extends AbstractRule.Builder<B, R> {

        protected Builder(R rule) {
            super(rule);
        }

        public B severity(Severity severity) {
            AbstractSeverityRule r = get();
            r.severity = severity;
            return builder();
        }
    }
}
