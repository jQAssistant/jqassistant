package com.buschmais.jqassistant.plugin.common.impl.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
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

import static com.buschmais.jqassistant.core.report.api.ReportHelper.toColumn;
import static com.buschmais.jqassistant.core.report.api.ReportHelper.toRow;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(result).isNotNull();
        assertThat(result.getRule()).isEqualTo(concept);
        assertThat(result.getSeverity()).isEqualTo(Severity.MAJOR);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getColumnNames()).isEqualTo(asList("Property", "Value"));
        List<Row> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(1);
        Map<String, Column<?>> row = rows.get(0).getColumns();
        assertThat(row.get("Property").getValue()).isEqualTo("testProperty");
        assertThat(row.get("Value").getValue()).isEqualTo("testValue");
    }

    /**
     * Test {@link JavaRule} that returns the given configuration.
     */
    public static class TestJavaRule implements JavaRule {
        @Override
        public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> configuration, Map<String, Object> ruleParameters,
                Severity severity, AnalyzerContext context) {
            List<Row> rows = new ArrayList<>();
            for (Map.Entry<String, Object> entry : configuration.entrySet()) {
                Map<String, Column<?>> columns = new HashMap<>();
                columns.put("Property", toColumn(entry.getKey()));
                columns.put("Value", toColumn(entry.getValue()));
                rows.add(toRow(executableRule, columns));
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
