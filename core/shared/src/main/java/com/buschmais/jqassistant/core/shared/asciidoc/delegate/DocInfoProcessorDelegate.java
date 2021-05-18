package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.DocinfoProcessor;

public class DocInfoProcessorDelegate extends DocinfoProcessor {

    private final DocinfoProcessor delegate;

    public DocInfoProcessorDelegate(DocinfoProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public String process(Document document) {
        return delegate.process(document);
    }

}
