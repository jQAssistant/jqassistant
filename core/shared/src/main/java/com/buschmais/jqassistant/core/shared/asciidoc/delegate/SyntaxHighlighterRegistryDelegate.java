package com.buschmais.jqassistant.core.shared.asciidoc.delegate;

import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterRegistry;

public class SyntaxHighlighterRegistryDelegate implements SyntaxHighlighterRegistry {

    private final SyntaxHighlighterRegistry delegate;

    public SyntaxHighlighterRegistryDelegate(SyntaxHighlighterRegistry delegate) {
        this.delegate = delegate;
    }

    @Override
    public void register(Class<? extends SyntaxHighlighterAdapter> aClass, String... strings) {
        delegate.register(aClass, strings);
    }
}
