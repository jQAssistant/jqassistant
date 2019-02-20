package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.InlineMacroProcessor;

class InlineMacroProcessorDelegate extends InlineMacroProcessor {

    private final InlineMacroProcessor delegate;

    InlineMacroProcessorDelegate(InlineMacroProcessor delegate) {
        super(delegate.getName());
        this.delegate = delegate;
    }

    @Override
    public Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        return delegate.process(parent, target, attributes);
    }
}
