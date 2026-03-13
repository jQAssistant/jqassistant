package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.stream.Stream;

import org.jqassistant.schema.plugin.v2.JqassistantPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.never;

class PluginIdGeneratorTest {
    static PluginIdGenerator generator = new PluginIdGenerator();

    // todo null
    // todo ""
    @ParameterizedTest
    @MethodSource("parameterProvider")
    void idGenerationWorksAsExpected(String name, String id, String expected) {
        JqassistantPlugin plugin = Mockito.mock(JqassistantPlugin.class);

        Mockito.when(plugin.getName()).thenReturn(name);
        Mockito.when(plugin.getId()).thenReturn(id);

        JqassistantPlugin apply = generator.apply(plugin);

        Mockito.verify(apply).setId(expected);
    }

    @Test
    void pluginWithIdWithoutAnyWhitespaceWillNotBeModified() {
        JqassistantPlugin plugin = Mockito.mock(JqassistantPlugin.class);

        Mockito.when(plugin.getName()).thenReturn("Time Maschine Plugin");
        Mockito.when(plugin.getId()).thenReturn("org.timed.plugin");

        JqassistantPlugin apply = generator.apply(plugin);

        Mockito.verify(apply, never()).setId(Mockito.anyString());
    }


    static Stream<Arguments> parameterProvider() {
        return Stream.of(arguments("Time Machine Plugin", null, "time_machine_plugin"),
                         arguments("Time Machine\tPlugin", null, "time_machine_plugin"),
                         arguments("Time Machine\t\tPlugin", null, "time_machine_plugin"),
                         arguments("Time Machine\t\tPlugin", null, "time_machine_plugin"),
                         arguments(" Time Machine\tPlugin\t ", null, "_time_machine_plugin_"),
                         arguments("Time \013Machine\t\tPlugin", null, "time_machine_plugin"));

    }
}
