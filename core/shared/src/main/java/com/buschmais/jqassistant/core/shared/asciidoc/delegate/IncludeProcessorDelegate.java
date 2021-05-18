package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;

class IncludeProcessorDelegate extends IncludeProcessor {

    private final IncludeProcessor delegate;

    IncludeProcessorDelegate(IncludeProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean handles(String target) {
        return delegate != null ? delegate.handles(target) : false;
    }

    @Override
    public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        if (delegate != null) {
            delegate.process(document, reader, target, attributes);
        }
    }
}
