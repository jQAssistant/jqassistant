package com.buschmais.jqassistant.core.rule.api.reader;

import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MAJOR;
import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MINOR;
import static lombok.AccessLevel.PRIVATE;

import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import lombok.*;

@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class RuleConfiguration {

    public static final RuleConfiguration DEFAULT = new RuleConfiguration();

    public static RuleConfigurationBuilder builder() {
        return DEFAULT.toBuilder();
    }

    private Severity defaultGroupSeverity = null;

    private Severity defaultConceptSeverity = MINOR;

    private Severity defaultConstraintSeverity = MAJOR;

}
