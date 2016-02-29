package com.buschmais.jqassistant.sonar.plugin.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.sonar.sonarrules.JQAssistantSonarRulesPlugin;
import com.buschmais.jqassistant.sonar.sonarrules.profile.JQAssistantProfileExporter;
import com.buschmais.jqassistant.sonar.sonarrules.rule.JQAssistantRuleRepository;

public class PluginTest {

    @Test
    public void extensions() {
        JQAssistantSonarRulesPlugin plugin = new JQAssistantSonarRulesPlugin();
        List<?> extensions = plugin.getExtensions();
        assertThat(extensions.contains(JQAssistantRuleRepository.class), equalTo(true));
        assertThat(extensions.contains(JQAssistantProfileExporter.class), equalTo(true));
    }
}
