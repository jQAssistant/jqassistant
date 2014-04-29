package com.buschmais.jqassistant.sonar.plugin.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.platform.ComponentContainer;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantSensor;

/**
 * Verifies the functionality of the
 * {@link com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantSensor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SensorTest {

    private JQAssistantSensor sensor;

    @Mock
    private RulesProfile rulesProfile;
    @Mock
    private ResourcePerspectives resourcePerspectives;
    @Mock
    private ComponentContainer componentContainer;
    @Mock
    private Settings settings;
    @Mock
    private ModuleFileSystem moduleFileSystem;
    @Mock
    private Project project;
    @Mock
    private SensorContext sensorContext;

    @Before
    public void createSensor() throws JAXBException {
        String ruleId = "example:FieldsMustBeReadOnly";
        Rule rule = Rule.create(JQAssistant.KEY, ruleId, ruleId);
        ActiveRule activeRule = mock(ActiveRule.class);
        when(activeRule.getRule()).thenReturn(rule);
        when(rulesProfile.getActiveRulesByRepository(JQAssistant.KEY)).thenReturn(Arrays.asList(activeRule));
        sensor = new JQAssistantSensor(rulesProfile, resourcePerspectives, componentContainer, settings, moduleFileSystem);
    }

    @Test
    public void readReport() {
        String reportFile = SensorTest.class.getResource("/jqassistant-report.xml").getFile();
        when(settings.getString(JQAssistant.SETTINGS_KEY_REPORT_PATH)).thenReturn(reportFile);
        when(moduleFileSystem.buildDir()).thenReturn(new File(reportFile));
        sensor.analyse(project, sensorContext);
    }
}
