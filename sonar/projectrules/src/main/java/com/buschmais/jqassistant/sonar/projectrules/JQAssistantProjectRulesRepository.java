package com.buschmais.jqassistant.sonar.projectrules;

import org.sonar.api.rule.Severity;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.java.Java;

import com.buschmais.jqassistant.sonar.plugin.JQAssistant;

/**
 * Dummy repository to define one placeholder rule, used to assign all violations coming from
 * a {@link com.buschmais.jqassistant.sonar.plugin.JQAssistant#SETTINGS_VALUE_DEFAULT_REPORT_FILE_PATH jQAssistant report} in a local project.
 */
public final class JQAssistantProjectRulesRepository implements RulesDefinition {

	public final static String GENERIC_CONSTRAINT_KEY = "structural problem";

	static final String RULE_NAME = "Placeholder for project specific violations";

	@Override
	public void define(Context context) {
		final NewRepository newRepository = context.createRepository(JQAssistant.KEY, Java.KEY);
		newRepository.setName(JQAssistant.NAME);

		final NewRule rule = newRepository.createRule(GENERIC_CONSTRAINT_KEY);
		rule.setName(RULE_NAME);
		rule.setInternalKey(GENERIC_CONSTRAINT_KEY);
		rule.setSeverity(Severity.MAJOR);
		rule.setMarkdownDescription("This rule must be activated for every project receiving violations from jQAssistant.");

		newRepository.done();
	}

}
