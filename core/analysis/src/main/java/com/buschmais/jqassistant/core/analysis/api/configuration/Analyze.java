package com.buschmais.jqassistant.core.analysis.api.configuration;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.analyze")
public interface Analyze {

    /**
     * The {@link Rule} configuration.
     *
     * @return The {@link Rule} configuration.
     */
    @Description("The rule configuration.")
    Rule rule();

    /**
     * The {@link Report} configuration.
     *
     * @return The {@link Report} configuration.
     */
    @Description("The report configuration.")
    Report report();

    String RULE_PARAMETERS = "rule-parameters";

    @Description("The parameters to be passed to rules.")
    Map<String, String> ruleParameters();

    String EXECUTE_APPLIED_CONCEPTS = "execute-applied-concepts";

    @Description("Execute concepts even if they have already been applied before.")
    @WithDefault("false")
    boolean executeAppliedConcepts();

    String WARN_ON_EXECUTION_TIME_SECONDS = "warn-on-rule-execution-time-seconds";

    @Description("The execution time [seconds] for rules (concepts/constraints) to show a warning. Can be used as a hint for optimization.")
    @WithDefault("5")
    int warnOnExecutionTimeSeconds();

}
