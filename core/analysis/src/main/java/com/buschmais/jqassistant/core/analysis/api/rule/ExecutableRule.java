package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines the interface for rules which can be executed on the database.
 */
public interface ExecutableRule extends Rule {

    /*
     * Return the severity.
     * 
     * @return The severity.
     */
    Severity getSeverity();

    /**
     * Return the (optional) deprecation message.
     * 
     * @return The deprecation message.
     */
    String getDeprecation();

    /**
     * Return the ids of required concepts.
     * 
     * @return The ids of required concepts.
     */
    Set<String> getRequiresConcepts();

    /**
     * Return the cypher query.
     * 
     * @return The cypher query.
     */
    String getCypher();

    /**
     * Return the executable script
     * 
     * @return The executable script.
     */
    Script getScript();

    /**
     * Return the id of the template.
     * 
     * @return The template.
     */
    String getTemplateId();

    /**
     * Return the map of parameters.
     * 
     * @return The parameters.
     */
    Map<String, Object> getParameters();

    /**
     * Return the result verification for this rule.
     * 
     * @return The result verification.
     */
    Verification getVerification();
}
