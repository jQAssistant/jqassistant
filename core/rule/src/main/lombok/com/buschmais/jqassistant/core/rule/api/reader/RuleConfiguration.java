package com.buschmais.jqassistant.core.rule.api.reader;

import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MAJOR;
import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MINOR;
import static lombok.AccessLevel.PRIVATE;

import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class RuleConfiguration {

    private static final RuleConfiguration PROTOTYPE = new RuleConfiguration();

    public static RuleConfigurationBuilder builder() {
        return PROTOTYPE.toBuilder();
    }

    private Severity defaultGroupSeverity = null;

    private Severity defaultConceptSeverity = MINOR;

    private Severity defaultConstraintSeverity = MAJOR;

}
