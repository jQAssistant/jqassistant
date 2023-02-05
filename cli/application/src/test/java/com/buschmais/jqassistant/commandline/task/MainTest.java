package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.commandline.TaskFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void skipBySystemProperty() throws CliExecutionException {
        main.run(new String[] { "test" });

        verify(task, never()).run(any());
    }

    @Test
    @SetEnvironmentVariable(key = "JQASSISTANT_SKIP", value = "true")
    void skipByEnvironmentVariable() throws CliExecutionException {
        main.run(new String[] { "test" });

        verify(task, never()).run(any());
    }
}
