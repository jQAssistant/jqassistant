package com.buschmais.jqassistant.sonar.sonarrules;

import static java.util.Arrays.asList;

import java.util.List;

import org.sonar.api.SonarPlugin;

import com.buschmais.jqassistant.sonar.sonarrules.profile.JQAssistantProfileExporter;
import com.buschmais.jqassistant.sonar.sonarrules.rule.IdentityRuleKeyResolver;
import com.buschmais.jqassistant.sonar.sonarrules.rule.JQAssistantRuleRepository;

/**
 * Defines the jQAssistant plugin.
 */
public class JQAssistantSonarRulesPlugin extends SonarPlugin {

    /**
     * Return the plugin extensions.
     * 
     * @return The plugin extensions.
     */
    @SuppressWarnings("rawtypes")
    public List getExtensions() {
        return asList(JQAssistantRuleRepository.class, IdentityRuleKeyResolver.class, JQAssistantProfileExporter.class);
    }
}
