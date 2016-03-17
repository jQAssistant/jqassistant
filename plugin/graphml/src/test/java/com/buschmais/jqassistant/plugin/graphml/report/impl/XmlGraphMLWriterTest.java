package com.buschmais.jqassistant.plugin.graphml.report.impl;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Report;
import com.buschmais.jqassistant.core.shared.reflection.ClassHelper;
import com.buschmais.jqassistant.plugin.graphml.report.decorator.YedGraphMLDecorator;
import com.buschmais.jqassistant.plugin.graphml.test.CustomGraphMLDecorator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class XmlGraphMLWriterTest {

    @Mock
    private ClassHelper classHelper;

    @Mock
    private Concept concept;

    @Mock
    private Result<?> result;

    @Before
    public void setUp() {
        when(result.getRule()).thenReturn(concept);
    }

    @Test
    public void ruleSpecificDecorator() throws IOException, XMLStreamException {
        Report report = Report.Builder.newInstance().property("graphml.report.decorator", CustomGraphMLDecorator.class.getName()).get();
        when(concept.getReport()).thenReturn(report);
        CustomGraphMLDecorator mock = mock(CustomGraphMLDecorator.class);
        doReturn(CustomGraphMLDecorator.class).when(classHelper).getType(CustomGraphMLDecorator.class.getName());
        when(classHelper.createInstance(CustomGraphMLDecorator.class)).thenReturn(mock);

        File file = File.createTempFile("test", ".graphml");
        file.deleteOnExit();
        Map<String, Object> properties = new HashMap<>();
        XmlGraphMLWriter writer = new XmlGraphMLWriter(classHelper, YedGraphMLDecorator.class, properties);
        SubGraphImpl subGraph = new SubGraphImpl();

        writer.write(result, subGraph, file);

        verify(classHelper).getType(CustomGraphMLDecorator.class.getName());
        verify(classHelper).createInstance(CustomGraphMLDecorator.class);
    }
}
