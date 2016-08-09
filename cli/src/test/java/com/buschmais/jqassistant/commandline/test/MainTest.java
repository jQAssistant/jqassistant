package com.buschmais.jqassistant.commandline.test;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.commandline.TaskFactory;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        File propertyFile = ClasspathResource.getFile(MainTest.class, "/jqassistant-alternative.properties");
        Main main = new Main(taskFactory);
        main.run(new String[] { "test", "-p", propertyFile.getAbsolutePath() });
        verifyPropertyValue("alternativeValue");
    }

    private void verifyPropertyValue(String expectedValue) throws CliExecutionException {
        ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(task).initialize(any(PluginRepository.class), propertiesCaptor.capture());
        Map properties = propertiesCaptor.getValue();
        assertThat(properties.get("testKey"), CoreMatchers.<Object> equalTo(expectedValue));
    }

}
