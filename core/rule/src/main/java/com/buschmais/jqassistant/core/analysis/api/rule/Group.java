package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

/**
 * Defines a group.
 */
@Getter
@SuperBuilder
public class Group extends AbstractSeverityRule {

    /**
     * The set of rules contained in the group.
     */
    @Singular
    private Map<String, Severity> concepts = new LinkedHashMap<>();

    /**
     * The set of constraints contained in the group.
     */
    @Singular
    private Map<String, Severity> constraints = new LinkedHashMap<>();

    /**
     * The set of groups contained in the group.
     */
    @Singular
    private Map<String, Severity> groups = new LinkedHashMap<>();

}
