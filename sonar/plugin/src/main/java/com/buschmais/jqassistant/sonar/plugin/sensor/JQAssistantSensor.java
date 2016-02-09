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
import org.sonar.api.platform.ComponentContainer;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;

import com.buschmais.jqassistant.core.report.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.report.schema.v1.ConstraintType;
import com.buschmais.jqassistant.core.report.schema.v1.GroupType;
import com.buschmais.jqassistant.core.report.schema.v1.JqassistantReport;
import com.buschmais.jqassistant.core.report.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.report.schema.v1.RuleType;
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
	private final JAXBContext reportContext;
	private final JQAssistantConfiguration configuration;
	private final RuleKeyResolver ruleResolver;

	private final IssueConceptHandler conceptHandler;
	private final IssueConstraintHandler constraintHandler;

	public JQAssistantSensor(JQAssistantConfiguration configuration, ResourcePerspectives perspectives, ComponentContainer componentContainerc,
			FileSystem moduleFileSystem) throws JAXBException {
		this.configuration = configuration;
		this.fileSystem = moduleFileSystem;
		Map<String, LanguageResourceResolver> languageResourceResolvers = new HashMap<>();
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
			this.ruleResolver = null;
			//this situation should never happen, because the both plugins are using the same repository name preventing the the start of SonarQ from the clash
			LOGGER.error("Found more than one {} implementation. Uninstall one of the providing plugins ('sonarrules' or 'projectrules')", RuleKeyResolver.class.getSimpleName());
		}
		else
		{
			//only one present, perfect
			this.ruleResolver = ruleResolvers.get(0);
		}

		this.reportContext = JAXBContext.newInstance(ObjectFactory.class);
		this.conceptHandler = new IssueConceptHandler(perspectives, languageResourceResolvers);
		this.constraintHandler = new IssueConstraintHandler(perspectives, languageResourceResolvers);
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
		File reportFile = findReportFile(project, "");
		if (reportFile != null) {
			LOGGER.debug("Use report found at '{}'.", reportFile.getAbsolutePath());
			JqassistantReport report = readReport(reportFile);
			evaluateReport(project, sensorContext, report);
		}
		else
		{
			LOGGER.info("No report found at {} for project {}... (do nothing).", determineConfiguredReportPath(), project.getName());
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
					LOGGER.warn("Cannot resolve rule key for id '{}'. No issue will be created! Rule not active?", id);
					continue;
				}
				if (ruleType instanceof ConceptType) {
					if(configuration.suppressConceptFailures()) {
						continue;
					}
					conceptHandler.process(project, sensorContext, (ConceptType) ruleType, ruleKey);
				} else if (ruleType instanceof ConstraintType) {
					constraintHandler.process(project, sensorContext,(ConstraintType) ruleType, ruleKey);
				}
			}
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
	private File findReportFile(Project project, String pathPrefix) {
		if(project == null) {
			return null;
		}
		String configReportPath = determineConfiguredReportPath();
		//for untouched project the file system is not available, so we have to navigate via hardcoded '../' to parent projects
		//TODO: Is there a alternative to '../' to resolve parent project paths?
		File reportFile = fileSystem.resolvePath(pathPrefix+configReportPath);
		if (reportFile.exists()) {
			return reportFile;
		}
		if(project.isModule()) {
			return findReportFile(project.getParent(), pathPrefix+"../");
		}
		return null;
	}

	/**
	 * The path is relative or absolute.
	 */
	private String determineConfiguredReportPath()
	{
		String configReportPath = configuration.getReportPath();
		if(configReportPath == null || configReportPath.isEmpty()) {
			configReportPath = JQAssistant.SETTINGS_VALUE_DEFAULT_REPORT_FILE_PATH;
		}
		return configReportPath;
	}
}
