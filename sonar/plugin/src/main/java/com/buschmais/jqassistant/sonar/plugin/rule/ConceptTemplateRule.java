package com.buschmais.jqassistant.sonar.plugin.rule;

import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(key = "ConceptTemplate", name = "Concept Template", description = "Template for user a defined concept.", priority = Priority.MAJOR, cardinality = Cardinality.MULTIPLE)
public class ConceptTemplateRule extends AbstractTemplateRule {
}
