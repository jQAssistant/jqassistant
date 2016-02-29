package com.buschmais.jqassistant.sonar.plugin.sensor;

import org.sonar.api.BatchExtension;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;

/**
 * Abstraction to assign found jQAssistant violations to a concrete (customizable) SonarQ rule.
 * 
 * @author rzozmann
 *
 */
public interface RuleKeyResolver extends BatchExtension {

	/**
	 * 
	 * @param project The project to process.
	 * @param type The type of rule (concept or constraint).
	 * @param jQAssistantId The id of concept/rule.
	 * @return The SonarQ rule key or <code>null</code> if no rule for <i>jQAssistantId</i> exists.
	 */
	public RuleKey resolve(Project project, JQAssistantRuleType type, String jQAssistantId);
}
