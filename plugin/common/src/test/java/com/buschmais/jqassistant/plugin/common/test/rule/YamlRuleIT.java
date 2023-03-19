package com.buschmais.jqassistant.plugin.common.test.rule;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Verifies YAML rule execution.
 */
public class YamlRuleIT extends AbstractPluginIT {

    @Test
    public void adocJavaRule() throws RuleException {
        verify("adoc-java-rule:Concept");
    }

    @Test
    public void xmlJavaRule() throws RuleException {
        verify("xml-java-rule:Concept");
    }

    private void verify(String conceptId) throws RuleException {
        assertThat(applyConcept(conceptId).getStatus(), equalTo(SUCCESS));
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
                    Map<String, Object> row = new HashMap<>();
                    row.put("Property", propertyDescriptor);
                    return Result.<T>builder()
                        .rule(executableRule)
                        .severity(severity)
                        .columnNames(singletonList("Property"))
                        .rows(singletonList(row))
                        .status(SUCCESS)
                        .build();
                });
        }
    }
}
