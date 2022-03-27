package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.commandline.TaskFactory;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies functionality of the main class.
 */
@ExtendWith(MockitoExtension.class)
@ClearSystemProperty(key = "jqassistant.skip") // might be given as system property to Maven
class MainTest {

    @Mock
    private TaskFactory taskFactory;

    @Mock
    private Task task;

    private Main main;

    @BeforeEach
    void setUp() throws CliExecutionException {
        when(taskFactory.fromName("test")).thenReturn(task);
        this.main = new Main(taskFactory);
    }

    @Test
    @SetSystemProperty(key = "jqassistant.skip", value = "true")
    void skip() throws CliExecutionException {
        main.run(new String[] { "test" });

        verify(task, never()).run(any());
    }

    @Test
    void defaultPluginProperties() throws CliExecutionException {
        main.run(new String[] { "test" });

        verifyPropertyValue("testValue");
    }

    @Test
    void alternativePluginProperties() throws CliExecutionException {
        File propertyFile = ClasspathResource.getFile(MainTest.class, "/jqassistant-alternative.properties");

        main.run(new String[] { "test", "-p", propertyFile.getAbsolutePath() });

        verifyPropertyValue("alternativeValue");
    }

    private void verifyPropertyValue(String expectedValue) throws CliExecutionException {
        ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(task).initialize(any(PluginRepository.class), propertiesCaptor.capture());
        Map properties = propertiesCaptor.getValue();
        assertThat(properties.get("testKey")).isEqualTo(expectedValue);
    }

}
