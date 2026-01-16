package com.buschmais.jqassistant.core.analysis.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes an executed group.
 */
@Label("Group")
public interface GroupDescriptor extends RuleDescriptor, RuleGroupTemplate {

    @Relation("OVERRIDES_GROUP")
    GroupDescriptor getOverridesGroup();

    void setOverridesGroup(GroupDescriptor groupDescriptor);
}
