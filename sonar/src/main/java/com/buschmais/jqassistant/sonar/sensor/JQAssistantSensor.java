package com.buschmais.jqassistant.sonar.sensor;

import com.buschmais.jqassistant.core.report.schema.v1.*;
import com.buschmais.jqassistant.sonar.JQAssistant;
import com.buschmais.jqassistant.sonar.rule.JQAssistantRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
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

public class JQAssistantSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JQAssistantSensor.class);

    private final AnnotationCheckFactory annotationCheckFactory;

    private final JAXBContext reportContext;

    private final Map<String, Rule> rules;

    public JQAssistantSensor(RulesProfile profile) throws JAXBException {
        this.annotationCheckFactory = AnnotationCheckFactory.create(profile, JQAssistant.KEY, JQAssistantRuleRepository.RULE_CLASSES);
        this.reportContext = JAXBContext.newInstance(ObjectFactory.class);
        this.rules = new HashMap<String, Rule>();
        for (Object check : annotationCheckFactory.getChecks()) {
            Rule rule = annotationCheckFactory.getActiveRule(check).getRule();
            rules.put(rule.getName(), rule);
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
                Rule rule = rules.get(id);
                if (rule == null) {
                    LOGGER.warn("Cannot resolve rule for id '{}'.", id);
                } else {
                    ResultType result = ruleType.getResult();
                    boolean hasRows = result != null && result.getRows().getCount() > 0;
                    if (ruleType instanceof ConceptType && !hasRows) {
                        createViolation(project, sensorContext, rule);
                    } else if (ruleType instanceof ConstraintType && hasRows) {
                        createViolation(project, sensorContext, rule);
                    }
                }
            }
        }
    }

    private void createViolation(Project project, SensorContext sensorContext, Rule rule) {
        Violation violation = Violation.create(rule, project);
        sensorContext.saveViolation(violation);
    }
}
