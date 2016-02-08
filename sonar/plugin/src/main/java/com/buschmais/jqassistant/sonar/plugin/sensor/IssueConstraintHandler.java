package com.buschmais.jqassistant.sonar.plugin.sensor;

import java.util.Map;

import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable.IssueBuilder;

import com.buschmais.jqassistant.core.report.schema.v1.ColumnType;
import com.buschmais.jqassistant.core.report.schema.v1.ConstraintType;
import com.buschmais.jqassistant.core.report.schema.v1.RowType;

class IssueConstraintHandler extends AbstractIssueHandler<ConstraintType> {


	IssueConstraintHandler(ResourcePerspectives perspectives, Map<String, LanguageResourceResolver> languageResourceResolvers) {
		super(perspectives, languageResourceResolvers);
	}

	@Override
	protected boolean fillIssue(IssueBuilder issueBuilder, String ruleId, String ruleDescription, String primaryColumn, RowType rowEntry) {
		if(rowEntry == null) {
			return false;
		}
		String message = ruleId +": "+ruleDescription;
		String addMessage = buildMessage(rowEntry, primaryColumn);
		if(addMessage.length() > 1) {
			message = message.concat(" [" + addMessage.toString()+"]");
		}
		issueBuilder.message(message);
		return true;
	}

	private String buildMessage(RowType rowType, String primaryColumn)
	{
		StringBuilder message = new StringBuilder();
		for (ColumnType column : rowType.getColumn()) {
			String name = column.getName();
			String value = column.getValue();
			if (name.equals(primaryColumn)) {
				continue;
			}
			//log only additional columns, because the resource is already assigned with primary column
			if (message.length() > 0) {
				message.append(", ");
			}
			message.append(name);
			message.append('=');
			message.append(value);
		}
		return message.toString();
	}
}
