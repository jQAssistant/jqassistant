package com.buschmais.jqassistant.plugin.common.impl.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.plugin.common.api.rule.JavaRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class YamlRuleInterpreterPluginTest {

    @Mock
    private AnalyzerContext analyzerContext;

    private YamlRuleInterpreterPlugin plugin = new YamlRuleInterpreterPlugin();

    @BeforeEach
    public void setUp() {
        plugin.initialize();
        plugin.configure(emptyMap());
    }

    @Test
    public void javaRule() throws RuleException {
        String source = "java-rule: " + TestJavaRule.class.getName() + "\nconfiguration:\n  testProperty: testValue";
        SourceExecutable<String> executable = new SourceExecutable("yaml", source, String.class);
        Concept concept = Concept.builder().id("test-java-rule").executable(executable).severity(Severity.MINOR).build();

        Result<Concept> result = plugin.execute(concept, emptyMap(), Severity.MAJOR, analyzerContext);

        assertThat(result, notNullValue());
        assertThat(result.getRule(), is(concept));
        assertThat(result.getSeverity(), is(Severity.MAJOR));
        assertThat(result.getStatus(), equalTo(Result.Status.SUCCESS));
        assertThat(result.getColumnNames(), equalTo(asList("Property", "Value")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        assertThat(row.get("Property"), equalTo("testProperty"));
        assertThat(row.get("Value"), equalTo("testValue"));
    }

    /**
     * Test {@link JavaRule} that returns the given configuration.
     */
    public static class TestJavaRule implements JavaRule {
        @Override
        public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> configuration, Map<String, Object> ruleParameters,
                Severity severity, AnalyzerContext context) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Map.Entry<String, Object> entry : configuration.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("Property", entry.getKey());
                row.put("Value", entry.getValue());
                rows.add(row);
            }
            return Result.<T>builder()
                .rule(executableRule)
                .severity(severity)
                .status(Result.Status.SUCCESS)
                .columnNames(asList("Property", "Value"))
                .rows(rows)
                .build();
        }
    }
}
