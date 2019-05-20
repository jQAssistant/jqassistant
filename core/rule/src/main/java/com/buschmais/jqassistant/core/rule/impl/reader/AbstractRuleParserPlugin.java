package com.buschmais.jqassistant.core.rule.impl.reader;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Baseclass with common fuctionality which could by used for every implementation
 * of {@link RuleParserPlugin}.
 */
public abstract class AbstractRuleParserPlugin implements RuleParserPlugin {


    private RuleConfiguration ruleConfiguration;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRuleParserPlugin.class);

    protected static final String ROW_COUNT = "rowCount";
    protected static final String ROW_COUNT_MIN = "rowCountMin";
    protected static final String ROW_COUNT_MAX = "rowCountMax";

    protected static final String AGGREGATION = "aggregation";
    protected static final String AGGREGATION_COLUMN = "aggregationColumn";
    protected static final String AGGREGATION_MIN = "aggregationMin";
    protected static final String AGGREGATION_MAX = "aggregationMax";

    protected static final String PARAMETER_DEFAULT_VALUE = "defaultValue";
    protected static final String PARAMETER_NAME = "name";
    protected static final String PARAMETER_TYPE = "type";

    protected static final String CONCEPT = "concept";
    protected static final String CONCEPTS = "concepts";
    protected static final String CONSTRAINT = "constraint";
    protected static final String CONSTRAINTS = "constraints";
    protected static final String GROUP = "group";
    protected static final String GROUPS = "groups";

    protected static final String INCLUDES_GROUPS = "includesGroups";
    protected static final String INCLUDES_CONCEPTS = "includesConcepts";
    protected static final String INCLUDES_CONSTRAINTS = "includesConstraints";

    protected static final String SEVERITY = "severity";
    protected static final String DEPENDS = "depends";
    protected static final String REQUIRES_CONCEPTS = "requiresConcepts";
    protected static final String REQUIRES_PARAMETERS = "requiresParameters";
    protected static final String REPORT = "report";
    protected static final String REPORT_TYPE = "reportType";
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
        } catch (RuleException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("An unhandled exception occured while reading " +
                                            "rules from '" +
                                            ruleSource.getId() + "'", e);
        }
    }

    protected abstract void doParse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder)
        throws RuleException;

    @Override
    public void configure(RuleConfiguration ruleConfiguration) {
        this.ruleConfiguration = ruleConfiguration;
    }

    protected RuleConfiguration getRuleConfiguration() {
        return ruleConfiguration;
    }

}
