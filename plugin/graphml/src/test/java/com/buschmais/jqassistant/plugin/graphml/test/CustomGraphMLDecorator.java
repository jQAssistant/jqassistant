package com.buschmais.jqassistant.plugin.graphml.test;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.plugin.graphml.report.api.GraphMLDecorator;
import com.buschmais.jqassistant.plugin.graphml.report.decorator.YedGraphMLDecorator;
import com.buschmais.xo.api.CompositeObject;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.util.Map;

public class CustomGraphMLDecorator implements GraphMLDecorator {

    private GraphMLDecorator delegate = new YedGraphMLDecorator();

    @Override
    public void initialize(Result<?> result, XMLStreamWriter xmlWriter, File file, Map<String, Object> properties) {
        delegate.initialize(result, xmlWriter, file, properties);
    }

    @Override
    public Map<String, String> getNamespaces() {
        return delegate.getNamespaces();
    }

    @Override
    public Map<String, String> getSchemaLocations() {
        return delegate.getSchemaLocations();
    }

    @Override
    public void writeKeys() throws XMLStreamException {
        delegate.writeKeys();
    }

    @Override
    public void writeNodeAttributes(CompositeObject node) throws XMLStreamException {
        delegate.writeNodeAttributes(node);
    }

    @Override
    public void writeNodeElements(CompositeObject node) throws XMLStreamException {
        delegate.writeNodeElements(node);
    }

    @Override
    public void writeRelationshipAttributes(CompositeObject relationship) throws XMLStreamException {
        delegate.writeRelationshipAttributes(relationship);
    }

    @Override
    public void writeRelationshipElements(CompositeObject relationship) throws XMLStreamException {
        delegate.writeRelationshipElements(relationship);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
