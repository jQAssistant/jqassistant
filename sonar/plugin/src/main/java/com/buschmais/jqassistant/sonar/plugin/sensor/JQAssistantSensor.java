package com.buschmais.jqassistant.sonar.plugin.sensor;

import com.buschmais.jqassistant.core.report.schema.v1.*;
import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.rule.JQAssistantRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
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

    private final AnnotationCheckFactory annotationCheckFactory;

    private final JAXBContext reportContext;

    private final Map<String, ActiveRule> rules;

    public JQAssistantSensor(RulesProfile profile) throws JAXBException {
        this.annotationCheckFactory = AnnotationCheckFactory.create(profile, JQAssistant.KEY, JQAssistantRuleRepository.RULE_CLASSES);
        this.reportContext = JAXBContext.newInstance(ObjectFactory.class);
        this.rules = new HashMap<String, ActiveRule>();
        for (Object check : annotationCheckFactory.getChecks()) {
            ActiveRule rule = annotationCheckFactory.getActiveRule(check);
            rules.put(rule.getRule().getName(), rule);
        }
        for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
            rules.put(activeRule.getRule().getName(), activeRule);
        }
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
                        createViolation(project, sensorContext, activeRule, "The concept did not return a result.");
                    } else if (ruleType instanceof ConstraintType && hasRows) {
                        StringBuilder message = new StringBuilder();
                        for (RowType rowType : result.getRows().getRow()) {
                            StringBuilder row = new StringBuilder();
                            for (ColumnType columnType : rowType.getColumn()) {
                                if (row.length() > 0) {
                                    row.append(", ");
                                }
                                row.append(columnType.getName());
                                row.append('=');
                                row.append(columnType.getValue());
                            }
                            if (message.length() > 0) {
                                message.append('\n');
                            }
                            message.append(row);
                        }
                        createViolation(project, sensorContext, activeRule, message.toString());
                    }
                }
            }
        }
    }

    private void createViolation(Project project, SensorContext sensorContext, ActiveRule activeRule, String message) {
        Violation violation = Violation.create(activeRule, project);
        violation.setMessage(message);
        sensorContext.saveViolation(violation);
    }
}
