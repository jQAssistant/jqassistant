package com.buschmais.jqassistant.sonar.sonarrules.rule;

import java.util.HashMap;
import java.util.Map;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;

import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantRuleType;
import com.buschmais.jqassistant.sonar.plugin.sensor.RuleKeyResolver;

/**
 * 
 * @author rzozmann
 *
 */
public class IdentityRuleKeyResolver implements RuleKeyResolver {

	private final Map<String, ActiveRule> rules;
	
	public IdentityRuleKeyResolver(RulesProfile profile) {
		this.rules = new HashMap<>();
		for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
			//the key of the SonarQ rule is the id for jQAssistant
			rules.put(activeRule.getRule().getKey(), activeRule);
		}
	}

	@Override
	public RuleKey resolve(Project project, JQAssistantRuleType type, String jQAssistantId) {
		ActiveRule activeRule = rules.get(jQAssistantId);
		if(activeRule == null)
			return null;
		return activeRule.getRule().ruleKey();
	}

}
