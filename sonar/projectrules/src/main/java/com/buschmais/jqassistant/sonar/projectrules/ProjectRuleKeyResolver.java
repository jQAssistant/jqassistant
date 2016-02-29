package com.buschmais.jqassistant.sonar.projectrules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;

import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantRuleType;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantSensor;
import com.buschmais.jqassistant.sonar.plugin.sensor.RuleKeyResolver;

/**
 *
 * @author rzozmann
 *
 */
public class ProjectRuleKeyResolver implements RuleKeyResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(JQAssistantSensor.class);

	private final RuleKey ruleKey;
	public ProjectRuleKeyResolver(RulesProfile profile, RuleFinder ruleFinder) {

		final Rule rule = ruleFinder.findByKey(JQAssistant.KEY, JQAssistantProjectRulesRepository.GENERIC_CONSTRAINT_KEY);
		if(rule != null && profile.getActiveRule(rule) != null)
		{
			ruleKey = rule.ruleKey();
		}
		else
		{
			ruleKey = null;
		}
		//Remember: Activating of rule in a profile does not have a effect in database :-(
		if(ruleKey == null)
		{
			LOGGER.error("The rule '{}' is not active/present, no violation for {} can be reported in SonarQ", JQAssistantProjectRulesRepository.RULE_NAME, JQAssistant.NAME);
		}
	}

	@Override
	public RuleKey resolve(Project project, JQAssistantRuleType type, String jQAssistantId) {
		return ruleKey;
	}

}
