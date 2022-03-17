package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.rule.api.model.Severity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class RuleConfiguration {

    private Severity defaultGroupSeverity;

    private Severity defaultConceptSeverity;

    private Severity defaultConstraintSeverity;

}
