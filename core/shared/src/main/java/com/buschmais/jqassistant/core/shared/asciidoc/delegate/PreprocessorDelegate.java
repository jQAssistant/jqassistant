package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

public class PreprocessorDelegate extends Preprocessor {

    private final Preprocessor delegate;

    public PreprocessorDelegate(Preprocessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public PreprocessorReader process(Document document, PreprocessorReader reader) {
        return delegate.process(document, reader);
    }

    @Override
    public PreprocessorReader process(DocumentRuby document, PreprocessorReader reader) {
        return delegate.process(document, reader);
    }
}
