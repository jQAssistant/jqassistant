package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.function.Supplier;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Baseclass with common fuctionality which could by used for every
 * implementation of {@link RuleParserPlugin}.
 */
public abstract class AbstractRuleParserPlugin implements RuleParserPlugin {

    private Rule rule;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRuleParserPlugin.class);

    protected static final String ROW_COUNT = "rowCount";
    protected static final String ROW_COUNT_MIN = "min";
    protected static final String ROW_COUNT_MAX = "max";

    protected static final String AGGREGATION = "aggregation";
    protected static final String AGGREGATION_COLUMN = "column";
    protected static final String AGGREGATION_MIN = "min";
    protected static final String AGGREGATION_MAX = "max";

    protected static final String PARAMETER_DEFAULT_VALUE = "defaultValue";
    protected static final String PARAMETER_NAME = "name";
    protected static final String PARAMETER_TYPE = "type";

    protected static final String CONCEPT = "concept";
    protected static final String CONCEPTS = "concepts";
    protected static final String CONSTRAINT = "constraint";
    protected static final String CONSTRAINTS = "constraints";
    protected static final String GROUP = "group";
    protected static final String GROUPS = "groups";

    protected static final String INCLUDED_GROUPS = "includedGroups";
    protected static final String INCLUDED_CONCEPTS = "includedConcepts";
    protected static final String INCLUDED_CONSTRAINTS = "includedConstraints";

    protected static final String SEVERITY = "severity";
    protected static final String DEPENDS = "depends";
    protected static final String REQUIRES_CONCEPTS = "requiresConcepts";
    protected static final String PROVIDES_CONCEPTS = "providesConcepts";
    protected static final String REQUIRES_PARAMETERS = "requiresParameters";
    protected static final String REPORT = "report";
    protected static final String REPORT_TYPE = "type";
    protected static final String PRIMARY_COLUMN = "primaryColumn";
    protected static final String REPORT_PROPERTIES = "properties";
    protected static final String VERIFY = "verify";
    protected static final String TITLE = "title";
    protected static final String SOURCE = "source";
    protected static final String LANGUAGE = "language";
    protected static final String CYPHER = "cypher";
    protected static final String OPTIONAL = "optional";
    protected static final String DESCRIPTION = "description";
    protected static final String ID = "id";
    protected static final String REF_ID = "refId";

    @Override
    public void parse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException {
        LOGGER.debug("Reading rules from '{}'.", ruleSource.getId());

        try {
            doParse(ruleSource, ruleSetBuilder);
        } catch (RuntimeException e) {
            throw new RuleException("An unhandled exception occurred while reading rules from '" + ruleSource.getId() + "'", e);
        }
    }

    protected abstract void doParse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException;

    @Override
    public void configure(Rule rule) {
        this.rule = rule;
    }

    protected Severity getDefaultConceptSeverity() {
        return rule.defaultConceptSeverity()
            .orElse(Concept.DEFAULT_SEVERITY);
    }

    protected Severity getDefaultConstraintSeverity() {
        return rule.defaultConstraintSeverity()
            .orElse(Constraint.DEFAULT_SEVERITY);
    }

    protected Severity getDefaultGroupSeverity() {
        return rule.defaultGroupSeverity()
            .orElse(Group.DEFAULT_SEVERITY);
    }

    protected Severity getDefaultIncludeSeverity() {
        return rule.defaultGroupSeverity()
            .orElse(Group.DEFAULT_INCLUDE_SEVERITY);
    }

    protected Severity getSeverity(String value, Supplier<Severity> defaultSeveritySupplier) throws RuleException {
        Severity severity = Severity.fromValue(value);
        return severity != null ? severity : defaultSeveritySupplier.get();
    }
}
