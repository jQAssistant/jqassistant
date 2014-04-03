package com.buschmais.jqassistant.sonar.plugin.sensor;

import com.buschmais.jqassistant.core.report.schema.v1.*;
import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.rule.JQAssistantRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.SonarException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Sensor} implementation scanning for jqassistant-report.xml files.
 */
public class JQAssistantSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JQAssistantSensor.class);

    private final ResourcePerspectives perspectives;

    private final AnnotationCheckFactory annotationCheckFactory;

    private final Map<String, LanguageResourceResolver> languageResourceResolvers;

    private final Map<String, ActiveRule> rules;
    private final JAXBContext reportContext;

    public JQAssistantSensor(RulesProfile profile, ResourcePerspectives perspectives) throws JAXBException {
        this.perspectives = perspectives;
        this.annotationCheckFactory = AnnotationCheckFactory.create(profile, JQAssistant.KEY, JQAssistantRuleRepository.RULE_CLASSES);
        this.languageResourceResolvers = new HashMap<>();
        JavaResourceResolver javaResourceResolver = new JavaResourceResolver();
        this.languageResourceResolvers.put(javaResourceResolver.getLanguage(), javaResourceResolver);
        this.rules = new HashMap<>();
        for (Object check : annotationCheckFactory.getChecks()) {
            ActiveRule rule = annotationCheckFactory.getActiveRule(check);
            rules.put(rule.getRule().getName(), rule);
        }
        for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
            rules.put(activeRule.getRule().getName(), activeRule);
        }
        this.reportContext = JAXBContext.newInstance(ObjectFactory.class);
    }

    public void analyse(Project project, SensorContext sensorContext) {
        File buildDir = project.getFileSystem().getBuildDir();
        File reportFile = new File(buildDir, "jqassistant/jqassistant-report.xml");
        if (reportFile.exists()) {
            LOGGER.info("Reading report from " + reportFile);
            JqassistantReport report = readReport(reportFile);
            evaluateReport(project, sensorContext, report);
        }
    }

    public boolean shouldExecuteOnProject(Project project) {
        return true;
    }

    @Override
    public String toString() {
        return "jQAssistant Sensor";
    }

    private JqassistantReport readReport(File reportFile) {
        try {
            Unmarshaller unmarshaller = reportContext.createUnmarshaller();
            return unmarshaller.unmarshal(new StreamSource(reportFile), JqassistantReport.class).getValue();
        } catch (JAXBException e) {
            throw new SonarException("Cannot read jQAssistant report from file " + reportFile, e);
        }
    }

    private void evaluateReport(Project project, SensorContext sensorContext, JqassistantReport report) {
        for (GroupType groupType : report.getGroup()) {
            LOGGER.info("Processing group '{}'", groupType.getId());
            for (RuleType ruleType : groupType.getConceptOrConstraint()) {
                String id = ruleType.getId();
                ActiveRule activeRule = rules.get(id);
                if (activeRule == null) {
                    LOGGER.warn("Cannot resolve activeRule for id '{}'.", id);
                } else {
                    ResultType result = ruleType.getResult();
                    boolean hasRows = result != null && result.getRows().getCount() > 0;
                    if (ruleType instanceof ConceptType && !hasRows) {
                        createIssue(project, "The concept did not return a result.", activeRule, sensorContext);
                    } else if (ruleType instanceof ConstraintType && hasRows) {
                        for (RowType rowType : result.getRows().getRow()) {
                            StringBuilder message = new StringBuilder();
                            Resource<?> resource = null;
                            for (ColumnType columnType : rowType.getColumn()) {
                                String value = columnType.getValue();
                                // if a language element is found use it as a resource for creating an issue
                                String language = columnType.getLanguage();
                                if (language != null) {
                                    LanguageResourceResolver resourceResolver = languageResourceResolvers.get(language);
                                    if (resourceResolver != null) {
                                        String element = columnType.getElement();
                                        resource = resourceResolver.resolve(element, value);
                                    }
                                }
                                if (message.length() > 0) {
                                    message.append(", ");
                                }
                                message.append(columnType.getName());
                                message.append('=');
                                message.append(value);
                            }
                            if (resource == null) {
                                resource = project;
                            }
                            String issueDescription = ruleType.getDescription() + "\n" + message.toString();
                            createIssue(project, resource, issueDescription, activeRule, sensorContext);
                        }
                    }
                }
            }
        }

    }

    /**
     * Creates an issue.
     *
     * @param project       The project to create the issue for.
     * @param message       The message to use.
     * @param rule          The rule which has been violated.
     * @param sensorContext The sensor context.
     */
    private void createIssue(Project project, String message, ActiveRule rule, SensorContext sensorContext) {
        createIssue(project, project, message, rule, sensorContext);
    }

    /**
     * Creates an issue.
     *
     * @param project       The project to create the issue for.
     * @param message       The message to use.
     * @param rule          The rule which has been violated.
     * @param sensorContext The sensor context.
     */
    private void createIssue(Project project, Resource<?> resource, String message, ActiveRule rule, SensorContext sensorContext) {
        Issuable issuable;
        if (sensorContext.getResource(resource) != null) {
            issuable = perspectives.as(Issuable.class, resource);
        } else {
            LOGGER.warn("Resource '{}' not found.", resource.getLongName());
            issuable = perspectives.as(Issuable.class, (Resource<?>) project);
        }
        Issue issue = issuable.newIssueBuilder().ruleKey(rule.getRule().ruleKey()).message(message).build();
        issuable.addIssue(issue);
        LOGGER.info("Issue added for resource '{}'.", resource.getLongName());
    }
}
