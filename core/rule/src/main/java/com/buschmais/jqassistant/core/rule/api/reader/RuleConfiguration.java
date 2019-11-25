package com.buschmais.jqassistant.core.rule.api.reader;

import com.buschmais.jqassistant.core.rule.api.model.Severity;

import lombok.*;
import lombok.Builder.Default;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.MAJOR;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;
import static lombok.AccessLevel.PRIVATE;

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
