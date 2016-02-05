package com.buschmais.jqassistant.sonar.plugin.sensor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.platform.ComponentContainer;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;

import com.buschmais.jqassistant.core.report.schema.v1.ColumnHeaderType;
import com.buschmais.jqassistant.core.report.schema.v1.ColumnType;
import com.buschmais.jqassistant.core.report.schema.v1.ColumnsHeaderType;
import com.buschmais.jqassistant.core.report.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.report.schema.v1.ConstraintType;
import com.buschmais.jqassistant.core.report.schema.v1.ElementType;
import com.buschmais.jqassistant.core.report.schema.v1.GroupType;
import com.buschmais.jqassistant.core.report.schema.v1.JqassistantReport;
import com.buschmais.jqassistant.core.report.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.report.schema.v1.ResultType;
import com.buschmais.jqassistant.core.report.schema.v1.RowType;
import com.buschmais.jqassistant.core.report.schema.v1.RuleType;
import com.buschmais.jqassistant.core.report.schema.v1.SourceType;
import com.buschmais.jqassistant.core.report.schema.v1.StatusEnumType;
import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.JQAssistantConfiguration;

/**
 * {@link Sensor} implementation scanning for jqassistant-report.xml files.
 */
@Phase(name = Phase.Name.DEFAULT)
public class JQAssistantSensor implements Sensor {

	private static final Logger LOGGER = LoggerFactory.getLogger(JQAssistantSensor.class);

	//avoid multiple loading of report file (maybe a huge file!) while creation of new instance of sensor for a project
	//TODO: This works only if we have a single report, also in multi project environment
	private static JqassistantReport theReport = null;
	private static String theReportFilePath = null;

	private final FileSystem fileSystem;
	private final ResourcePerspectives perspectives;
	private final Map<String, LanguageResourceResolver> languageResourceResolvers;
	private final JAXBContext reportContext;
	private final JQAssistantConfiguration configuration;
	private final RuleKeyResolver ruleResolver;

	public JQAssistantSensor(JQAssistantConfiguration configuration, ResourcePerspectives perspectives, ComponentContainer componentContainerc,
			FileSystem moduleFileSystem) throws JAXBException {
		this.configuration = configuration;
		this.fileSystem = moduleFileSystem;
		this.perspectives = perspectives;
		this.languageResourceResolvers = new HashMap<>();
		for (LanguageResourceResolver resolver : componentContainerc.getComponentsByType(LanguageResourceResolver.class)) {
			languageResourceResolvers.put(resolver.getLanguage().toLowerCase(Locale.ENGLISH), resolver);
		}
		List<RuleKeyResolver> ruleResolvers = componentContainerc.getComponentsByType(RuleKeyResolver.class);
		if(ruleResolvers.isEmpty())
		{
			this.ruleResolver = null;
			LOGGER.error("{} will not work without additional plugin providing a {} implementation.", JQAssistant.NAME, RuleKeyResolver.class.getSimpleName());
		}
		else if(ruleResolvers.size() > 1)
		{
			this.ruleResolver = ruleResolvers.get(0);
			LOGGER.warn("Found more than one {} implementation, take the first one {}. Uninstall one of the providing plugins", RuleKeyResolver.class.getSimpleName(), ruleResolver.getClass().getSimpleName());
		}
		else
		{
			//only one present, perfect
			this.ruleResolver = ruleResolvers.get(0);
		}
		this.reportContext = JAXBContext.newInstance(ObjectFactory.class);
	}

	@Override
	public boolean shouldExecuteOnProject(Project project) {
		boolean disabled = configuration.isSensorDisabled();
		if(disabled)
		{
			LOGGER.info("{} is disabled on project {}", JQAssistant.NAME, project.getName());
		}
		else if(ruleResolver == null){
			disabled = true;
		}
		return !disabled;
	}

	@Override
	public void analyse(Project project, SensorContext sensorContext) {
		File reportFile = getReportFile(project, "");
		if (reportFile != null) {
			LOGGER.debug("Use report found at '{}'.", reportFile.getAbsolutePath());
			JqassistantReport report = readReport(reportFile);
			evaluateReport(project, sensorContext, report);
		}
		else
		{
			LOGGER.info("No report found at {} for project {}... (do nothing).", JQAssistant.SETTINGS_VALUE_DEFAULT_REPORT_FILE_PATH, project.getName());
		}
	}

	@Override
	public String toString() {
		return JQAssistant.NAME;
	}

	private JqassistantReport readReport(File reportFile) {
		if(theReport != null && reportFile.getAbsolutePath().equals(theReportFilePath)) {
			return theReport;
		}
		try {
			Unmarshaller unmarshaller = reportContext.createUnmarshaller();
			theReport = unmarshaller.unmarshal(new StreamSource(reportFile), JqassistantReport.class).getValue();
			theReportFilePath = reportFile.getAbsolutePath();
			return theReport;
		} catch (JAXBException e) {
			throw new IllegalStateException("Cannot read jQAssistant report from file " + reportFile, e);
		}
	}

	private void evaluateReport(Project project, SensorContext sensorContext, JqassistantReport report) {
		for (GroupType groupType : report.getGroup()) {
			LOGGER.info("Processing group '{}'", groupType.getId());
			for (RuleType ruleType : groupType.getConceptOrConstraint()) {
				if (!StatusEnumType.FAILURE.equals(ruleType.getStatus())) {
					continue;
				}
				final String id = ruleType.getId();
				final RuleKey ruleKey = ruleResolver.resolve(project, (ruleType instanceof ConceptType)? JQAssistantRuleType.Concept : JQAssistantRuleType.Constraint, id);
				if(ruleKey == null)
				{
					LOGGER.warn("Cannot resolve rule key for id '{}'. No issue will be created!", id);
					continue;
				}
				if (ruleType instanceof ConceptType) {
					if(configuration.suppressConceptFailures()) {
						continue;
					}
					createIssue(project, null, "The concept "+ruleType.getId()+" could not be applied.", id, ruleKey, sensorContext);
				} else if (ruleType instanceof ConstraintType) {
					handleConstraint(project,sensorContext, ruleType, ruleKey);
				}
			}
		}
	}

	private void handleConstraint(Project project, SensorContext sensorContext, RuleType ruleType, RuleKey ruleKey) {
		ResultType result = ruleType.getResult();
		String primaryColumn = getPrimaryColumn(result);
		for (RowType rowType : result.getRows().getRow()) {
			Resource resource = null;
			Integer lineNumber = null;
			StringBuilder message = new StringBuilder();
			//use project as anchor in case of not given resource
			resource = project;
			for (ColumnType column : rowType.getColumn()) {
				String name = column.getName();
				String value = column.getValue();
				if (name.equals(primaryColumn)) {
					ElementType languageElement = column.getElement();
					SourceType source = column.getSource();
					if (languageElement != null) {
						final LanguageResourceResolver resourceResolver = languageResourceResolvers.get(languageElement.getLanguage().toLowerCase(Locale.ENGLISH));
						if (resourceResolver != null) {
							String element = languageElement.getValue();
							resource = resourceResolver.resolve(project, element, source.getName(), value);
						}
					}
					lineNumber = source.getLine();
				}
				if (message.length() > 0) {
					message.append(", ");
				}
				message.append(name);
				message.append('=');
				message.append(value);
			}
			String issueDescription = ruleType.getDescription() + "\n" + message.toString();
			if(resource != null) {
				//report only violations for matching resources to avoid duplicated violations from other sub modules in same report
				createIssue(resource, lineNumber, issueDescription, ruleType.getId(),ruleKey, sensorContext);
			}
		}
	}

	/**
	 * Determine the primary column from the result, i.e. the column which
	 * contains the resource to create an issue for.
	 *
	 * @param result
	 *            The result.
	 * @return The name of the primary column or <code>null</code>.
	 */
	private String getPrimaryColumn(ResultType result) {
		if (result != null) {
			ColumnsHeaderType columns = result.getColumns();
			for (ColumnHeaderType columnHeaderType : columns.getColumn()) {
				if (columnHeaderType.isPrimary()) {
					return columnHeaderType.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Creates an issue.
	 *
	 * @param resourceResolved
	 *            The resource to create the issue for.
	 * @param message
	 *            The message to use.
	 * @param rule
	 *            The rule which has been violated.
	 * @param sensorContext
	 *            The sensor context.
	 */
	private void createIssue(Resource resourceResolved, Integer lineNumber, String message, String jQAssistantId, RuleKey ruleKey, SensorContext sensorContext) {
		Issuable issuable;
		Resource resourceIndex = sensorContext.getResource(resourceResolved);
		if (resourceIndex == null)
		{
			LOGGER.warn("Resource '{}' not found, issue not created.", resourceResolved.getPath());
			return;
		}
		issuable = perspectives.as(Issuable.class, resourceIndex);
		if(issuable == null)
		{
			LOGGER.warn("Ressource {} isn't issueable; create no violation!",resourceIndex.getPath());
			return;
		}
		Issuable.IssueBuilder issueBuilder = issuable.newIssueBuilder().ruleKey(ruleKey).message(message);
		if (lineNumber != null) {
			issueBuilder.line(lineNumber);
		}
		try
		{
			issuable.addIssue(issueBuilder.build());
			LOGGER.info("Issue '{}' added for resource '{}'.", jQAssistantId, (resourceIndex.getPath() != null ? resourceIndex.getPath() : resourceIndex.getName()));
		}
		catch(Exception ex)
		{
			LOGGER.error("Problem creating violation", ex);
		}
	}

	/**
	 * <ol>
	 * <li>Look for report file in current project dir</li>
	 * <li>if not found go to parent and look again (recursive up to root project)</li>
	 * </ol>
	 * Return the report xml file or null if not found. Checks whether
	 * {@link JQAssistant#SETTINGS_KEY_REPORT_PATH} is set or not and looks up
	 * the passed path or the default build directory.
	 *
	 * @return reportFile File object of report xml or null if not found.
	 */
	private File getReportFile(Project project, String pathPrefix) {
		if(project == null) {
			return null;
		}
		String configReportPath = configuration.getReportPath();
		if(configReportPath == null || configReportPath.isEmpty()) {
			configReportPath = JQAssistant.SETTINGS_VALUE_DEFAULT_REPORT_FILE_PATH;
		}
		//for untouched project the file system is not available, so we have to navigate via hardcoded '../' to parent projects
		//TODO: Is there a alternative to '../' to resolve parent project paths?
		File reportFile = fileSystem.resolvePath(pathPrefix+configReportPath);
		if (reportFile.exists()) {
			return reportFile;
		}
		if(project.isModule()) {
			return getReportFile(project.getParent(), "../");
		}
		return null;
	}
}
