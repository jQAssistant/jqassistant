package com.buschmais.jqassistant.sonar.projectrules;

import static java.util.Arrays.asList;

import java.util.List;

import org.sonar.api.SonarPlugin;

/**
 * Defines a sonar plugin.
 */
public class JQAssistantProjectRulesPlugin extends SonarPlugin {

	@SuppressWarnings("rawtypes")
	@Override
	public List getExtensions() {
		//    	return java.util.Collections.emptyList();
		return asList(ProjectRuleKeyResolver.class, JQAssistantProjectRulesRepository.class);
	}

}
