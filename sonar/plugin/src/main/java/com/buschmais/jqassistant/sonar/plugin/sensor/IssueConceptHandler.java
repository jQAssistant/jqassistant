package com.buschmais.jqassistant.sonar.plugin.sensor;

import java.util.Map;

import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable.IssueBuilder;

import com.buschmais.jqassistant.core.report.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.report.schema.v1.RowType;

class IssueConceptHandler extends AbstractIssueHandler<ConceptType> {


	IssueConceptHandler(ResourcePerspectives perspectives, Map<String, LanguageResourceResolver> languageResourceResolvers) {
		super(perspectives, languageResourceResolvers);
	}

	@Override
	protected SourceLocation determineAlternativeResource(
			RowType rowType) {
		if(rowType ==null) {
			//use project as anchor in case of not given resource
			return new SourceLocation(getProject(), null);
		}
		return null;
	}

	@Override
	protected boolean fillIssue(IssueBuilder issueBuilder, String ruleId, String ruleDescription, String primaryColumn, RowType rowEntry) {
		issueBuilder.message(ruleId +": The concept could not be applied.");
		return true;
	}
}
