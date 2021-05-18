package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;

class TreeProcessorDelegate extends Treeprocessor {

    private final Treeprocessor delegate;

    TreeProcessorDelegate(Treeprocessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public Document process(Document document) {
        return delegate.process(document);
    }

}
