package com.buschmais.jqassistant.scm.cli.test;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.Main;
import com.buschmais.jqassistant.scm.cli.Task;
import com.buschmais.jqassistant.scm.cli.TaskFactory;

/**
 * Verifies functionality of the main class.
 */
@RunWith(MockitoJUnitRunner.class)
public class MainTest {

    @Mock
    private TaskFactory taskFactory;

    @Mock
    private Task task;

    @Test
    public void defaultPluginProperties() throws CliExecutionException {
        when(taskFactory.fromName("test")).thenReturn(task);
        Main main = new Main(taskFactory);
        main.run(new String[] { "test" });
        verifyPropertyValue("testValue");
    }

    @Test
    public void alternativePluginProperties() throws CliExecutionException {
        when(taskFactory.fromName("test")).thenReturn(task);
        String propertyFilePath = MainTest.class.getResource("/jqassistant-alternative.properties").getPath();
        Main main = new Main(taskFactory);
        main.run(new String[] { "test", "-p", propertyFilePath });
        verifyPropertyValue("alternativeValue");
    }

    private void verifyPropertyValue(String expectedValue) throws CliExecutionException {
        ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(task).initialize(any(PluginRepository.class), propertiesCaptor.capture());
        Map properties = propertiesCaptor.getValue();
        assertThat(properties.get("testKey"), CoreMatchers.<Object> equalTo(expectedValue));
    }

}
