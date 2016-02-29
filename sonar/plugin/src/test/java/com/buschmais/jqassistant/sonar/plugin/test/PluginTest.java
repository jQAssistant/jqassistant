package com.buschmais.jqassistant.sonar.plugin.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.sonar.plugin.JQAssistantConfiguration;
import com.buschmais.jqassistant.sonar.plugin.JQAssistantPlugin;
import com.buschmais.jqassistant.sonar.plugin.language.JavaResourceResolver;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantSensor;

public class PluginTest {

    @Test
    public void extensions() {
        JQAssistantPlugin plugin = new JQAssistantPlugin();
        List<?> extensions = plugin.getExtensions();
        assertThat(extensions.contains(JQAssistantConfiguration.class), equalTo(true));
        assertThat(extensions.contains(JQAssistantSensor.class), equalTo(true));
        assertThat(extensions.contains(JavaResourceResolver.class), equalTo(true));
    }
}
