package com.buschmais.jqassistant.core.analysis.api.rule;

public class MetricGroupsBucket extends AbstractRuleBucket<MetricGroup, NoMetricGroupException, DuplicateMetricGroupException> {
    @Override
    protected String getRuleTypeName() {
        return "metric group";
    }

    @Override
    protected DuplicateMetricGroupException newDuplicateRuleException(String message) {
        return new DuplicateMetricGroupException(message);
    }

    @Override
    protected NoMetricGroupException newNoRuleException(String message) {
        return new NoMetricGroupException(message);
    }
}
