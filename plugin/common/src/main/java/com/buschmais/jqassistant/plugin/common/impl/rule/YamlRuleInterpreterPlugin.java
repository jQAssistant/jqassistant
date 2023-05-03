package com.buschmais.jqassistant.plugin.common.impl.rule;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.plugin.common.api.rule.JavaRule;

import lombok.Data;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static java.util.Collections.singletonList;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin}
 * the takes a YAML file containing a class as argument.
 */
public class YamlRuleInterpreterPlugin implements RuleInterpreterPlugin {

    @Data
    public static class YamlRuleSource {

        private String javaRule;

        private Map<String, Object> configuration;

    }

    @Override
    public Collection<String> getLanguages() {
        return singletonList("yaml");
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return executableRule.getExecutable() instanceof SourceExecutable && String.class.equals(executableRule.getExecutable().getType());
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context)
            throws RuleException {
        SourceExecutable<String> executable = (SourceExecutable<String>) executableRule.getExecutable();
        String source = executable.getSource();
        Constructor c = new Constructor(YamlRuleSource.class, new LoaderOptions());
        c.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) {
                return super.getProperty(type, LOWER_HYPHEN.to(LOWER_CAMEL, name));
            }
        });
        YamlRuleSource yamlRuleSource = new Yaml(c).loadAs(source, YamlRuleSource.class);
        String javaRuleClass = yamlRuleSource.getJavaRule();
        if (javaRuleClass == null) {
            throw new RuleException("'java-rule-class' is required.");
        }
        JavaRule javaRule = getJavaRule(javaRuleClass);
        return javaRule.execute(executableRule, yamlRuleSource.getConfiguration(), ruleParameters, severity, context);
    }

    private JavaRule getJavaRule(String className) throws RuleException {
        try {
            Class<?> javaRuleClass = Class.forName(className);
            if (!JavaRule.class.isAssignableFrom(JavaRule.class)) {
                throw new RuleException("Java '" + javaRuleClass.getName() + "' does not implement interface '" + JavaRule.class.getName() + "'.");
            }
            return JavaRule.class.cast(javaRuleClass.newInstance());
        } catch (ReflectiveOperationException e) {
            throw new RuleException("Cannot create instance of '" + className + "'.", e);
        }
    }
}
