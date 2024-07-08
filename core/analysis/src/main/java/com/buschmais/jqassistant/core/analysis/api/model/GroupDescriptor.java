package com.buschmais.jqassistant.core.analysis.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes an executed group.
 */
@Label("Group")
public interface GroupDescriptor extends RuleDescriptor, RuleGroupTemplate {
}
