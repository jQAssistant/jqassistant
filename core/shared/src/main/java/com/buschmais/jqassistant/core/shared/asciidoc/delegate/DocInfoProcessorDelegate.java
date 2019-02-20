package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
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

    @Override
    public String process(DocumentRuby documentRuby) {
        return delegate.process(documentRuby);
    }
}
