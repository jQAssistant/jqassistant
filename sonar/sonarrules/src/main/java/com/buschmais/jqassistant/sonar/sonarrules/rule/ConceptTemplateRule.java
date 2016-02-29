package com.buschmais.jqassistant.sonar.sonarrules.rule;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.squidbridge.annotations.RuleTemplate;

@Rule(key = "ConceptTemplate", name = "Concept Template", description = "Template for user a defined concept.", priority = Priority.MAJOR)
@RuleTemplate
public class ConceptTemplateRule extends AbstractTemplateRule {
	//no specific code
}
