package com.buschmais.jqassistant.sonar.rule;

import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(key = "ConstraintTemplate", name = "Constraint Template", description = "Template for user a defined constraint.", priority = Priority.CRITICAL, cardinality = Cardinality.MULTIPLE)
public class ConstraintTemplateRule extends AbstractTemplateRule {
}
