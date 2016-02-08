package com.buschmais.jqassistant.sonar.plugin.sensor;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;

import com.buschmais.jqassistant.core.report.schema.v1.ColumnHeaderType;
import com.buschmais.jqassistant.core.report.schema.v1.ColumnType;
import com.buschmais.jqassistant.core.report.schema.v1.ColumnsHeaderType;
import com.buschmais.jqassistant.core.report.schema.v1.ElementType;
import com.buschmais.jqassistant.core.report.schema.v1.ResultType;
import com.buschmais.jqassistant.core.report.schema.v1.RowType;
import com.buschmais.jqassistant.core.report.schema.v1.RuleType;
import com.buschmais.jqassistant.core.report.schema.v1.SourceType;

/**
 * Base class to produce a number of violations defined by instance of {@link T}.
 * @author rzozmann
 *
 */
abstract class AbstractIssueHandler<T extends RuleType> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(JQAssistantSensor.class);

	private final ResourcePerspectives perspectives;
	private final Map<String, LanguageResourceResolver> languageResourceResolvers;
	private SensorContext sensorContext = null;
	private Project project = null;

	protected AbstractIssueHandler(ResourcePerspectives perspectives, Map<String, LanguageResourceResolver> languageResourceResolvers) {
		this.perspectives = perspectives;
		this.languageResourceResolvers = languageResourceResolvers;
	}

	/**
	 *
	 * @return The current project, valid inside {@link #process(Project, SensorContext, RuleType, RuleKey)}.
	 */
	protected Project getProject() {
		return project;
	}

	/**
	 * Create 0..n violations, based on content and type of <i>ruleType</i>.
	 */
	public final void process(Project project,SensorContext sensorContext, T ruleType, RuleKey ruleKey)
	{
		this.project = project;
		this.sensorContext = sensorContext;
		ResultType result = ruleType.getResult();
		//'result' may be null for a) not applied (failed) concepts and b) successful constraints
		if(result == null)
		{
			final SourceLocation target = resolveRelatedResource(null, null);
			if(target == null || target.resource == null) {
				return;
			}
			//allow issue creation on project level
			handleIssueBuilding(target.resource, target.lineNumber, ruleType, ruleKey, null, null);
			return;
		}

		String primaryColumn = getPrimaryColumn(result);
		for (RowType rowType : result.getRows().getRow()) {
			final SourceLocation target = resolveRelatedResource(rowType, primaryColumn);
			//report only violations for matching resources to avoid duplicated violations from other sub modules in same report
			if(target == null || target.resource == null) {
				continue;
			}
			handleIssueBuilding(target.resource, target.lineNumber, ruleType, ruleKey, primaryColumn, rowType);
		}
	}

	private void handleIssueBuilding(Resource resourceResolved, Integer lineNumber, T ruleType, RuleKey ruleKey, String primaryColumn, RowType rowType)
	{
		Resource resourceIndex = sensorContext.getResource(resourceResolved);
		if (resourceIndex == null)
		{
			LOGGER.warn("Resource '{}' not found, issue not created.", resourceResolved.getPath());
			return;
		}
		final Issuable issuable = perspectives.as(Issuable.class, resourceIndex);
		if(issuable == null)
		{
			LOGGER.warn("Ressource {} isn't issueable; create no violation!",resourceIndex.getPath());
			return;
		}
		Issuable.IssueBuilder issueBuilder = issuable.newIssueBuilder().ruleKey(ruleKey);
		if (lineNumber != null) {
			issueBuilder.line(lineNumber);
		}
		try
		{
			String ruleId = ruleType.getId();
			boolean doIt = fillIssue(issueBuilder, ruleId, ruleType.getDescription(), primaryColumn, rowType);
			if(!doIt) {
				LOGGER.trace("Issue creation suppressed for {} on row {}", ruleId, rowType);
				return;
			}
			issuable.addIssue(issueBuilder.build());
			LOGGER.info("Issue '{}' added for resource '{}'.", ruleId, (resourceIndex.getPath() != null ? resourceIndex.getPath() : resourceIndex.getName()));
		}
		catch(Exception ex)
		{
			LOGGER.error("Problem creating violation", ex);
		}
	}

	/**
	 * Determine the primary column from the result, i.e. the column which contains the resource to create an issue for.
	 *
	 * @param result
	 *            The result.
	 * @return The name of the primary column or <code>null</code>.
	 */
	private String getPrimaryColumn(ResultType result) {
		if (result == null) {
			return null;
		}
		ColumnsHeaderType columns = result.getColumns();
		for (ColumnHeaderType columnHeaderType : columns.getColumn()) {
			if (!columnHeaderType.isPrimary()) {
				continue;
			}
			return columnHeaderType.getValue();
		}
		return null;
	}

	/**
	 * Helper method to lookup affected resource for given row of report entry.
	 * @return The resource or <code>null</code> if not found.
	 */
	private final SourceLocation resolveRelatedResource(RowType rowType, String primaryColumn) {
		if(rowType == null || primaryColumn == null) {
			return determineAlternativeResource(rowType);
		}
		for (ColumnType column : rowType.getColumn()) {
			String name = column.getName();
			if (!name.equals(primaryColumn)) {
				continue;
			}
			ElementType languageElement = column.getElement();
			if (languageElement == null) {
				return determineAlternativeResource(rowType);
			}
			SourceType source = column.getSource();
			final LanguageResourceResolver resourceResolver = languageResourceResolvers.get(languageElement.getLanguage().toLowerCase(Locale.ENGLISH));
			if (resourceResolver == null) {
				return determineAlternativeResource(rowType);
			}
			String element = languageElement.getValue();
			Resource resource = resourceResolver.resolve(project, element, source.getName(), column.getValue());
			if(resource == null) {
				return determineAlternativeResource(rowType);
			}
			return new SourceLocation(resource, source.getLine());
		}
		return determineAlternativeResource(rowType);
	}

	/**
	 * Client hook method to determine a resource for issue if default strategy fails.
	 * @param rowType The jQAssistant entry for row.<br/>
	 * The default implementation of this method does <code>return null</code>.
	 *
	 * @return The alternative location or <code>null</code>.
	 */
	protected SourceLocation determineAlternativeResource(RowType rowType)
	{
		return null;
	}

	/**
	 * Resource, line number and rule key are already set.
	 * @param issueBuilder The builder used to create the final {@link Issue issue}.
	 * @param ruleId The jQAssistant rule id.
	 * @param primaryColumn The name of the primary colum, maybe <code>null</code>.
	 * @param rowEntry Maybe <code>null</code> for not applied concepts.
	 * @return TRUE if issue should be created, FALSE to suppress issue creation.
	 */
	protected abstract boolean fillIssue(Issuable.IssueBuilder issueBuilder, String ruleId, String ruleDescription, String primaryColumn, RowType rowEntry);

}
