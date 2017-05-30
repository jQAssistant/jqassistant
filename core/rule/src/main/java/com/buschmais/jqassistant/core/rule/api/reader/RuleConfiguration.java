package com.buschmais.jqassistant.core.rule.api.reader;

import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MAJOR;
import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MINOR;
import static lombok.AccessLevel.PRIVATE;

import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import lombok.*;
import lombok.Builder.Default;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class RuleConfiguration {

    public static final RuleConfiguration DEFAULT = RuleConfiguration.builder().build();

    private Severity defaultGroupSeverity;

    @Default private Severity defaultConceptSeverity = MINOR;

    @Default private Severity defaultConstraintSeverity = MAJOR;

}
