package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

public class PreprocessorDelegate extends Preprocessor {

    private final Preprocessor delegate;

    public PreprocessorDelegate(Preprocessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {
        delegate.process(document, reader);
    }

}
