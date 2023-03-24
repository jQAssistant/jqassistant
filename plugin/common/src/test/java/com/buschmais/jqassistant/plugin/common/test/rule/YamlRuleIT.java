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
public class YamlRuleIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

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
        }
    }
}
