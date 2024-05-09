package com.buschmais.jqassistant.plugin.common.test.rule;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.common.api.rule.JavaRule;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies YAML rule execution.
 */
public class YamlRuleIT extends AbstractPluginIT {

    @Test
    public void xmlJavaRule() throws RuleException {
        assertThat(applyConcept("xml-java-rule:Concept").getStatus()).isEqualTo(SUCCESS);
    }

    public static class Concept implements JavaRule {

        @Override
        public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> properties, Map<String, Object> ruleParameters,
            Severity severity, AnalyzerContext context) {
            return context.getStore()
                .requireTransaction(() -> {
                    PropertyDescriptor propertyDescriptor = context.getStore()
                        .create(PropertyDescriptor.class);
                    propertyDescriptor.setName("testProperty");
                    propertyDescriptor.setValue("testValue");
            Map<String, Column<?>> columns = new HashMap<>();
            columns.put("Property", context.toColumn(propertyDescriptor));
                    return Result.<T>builder()
                        .rule(executableRule)
                        .severity(severity)
                        .columnNames(singletonList("Property"))
                .rows(singletonList(ReportHelper.toRow(executableRule, columns)))
                        .status(SUCCESS)
                        .build();
                });
        }
    }
}
