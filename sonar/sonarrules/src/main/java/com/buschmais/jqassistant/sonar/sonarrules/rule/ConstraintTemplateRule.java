package com.buschmais.jqassistant.sonar.sonarrules.rule;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.squidbridge.annotations.RuleTemplate;

@Rule(key = "ConstraintTemplate", name = "Constraint Template", description = "Template for user a defined constraint.", priority = Priority.CRITICAL)
@RuleTemplate
public class ConstraintTemplateRule extends AbstractTemplateRule {
	//no specific code
}
