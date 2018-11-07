package com.buschmais.jqassistant.plugin.json.impl.scanner;

import com.buschmais.jqassistant.plugin.json.impl.parser.JSONLexer;

import org.antlr.v4.runtime.CharStream;

/**
 * Lexer for JSON documents based on the generated JSON lexer by
 * ANTLR with configured listeners.
 */
public class JQAssistantJSONLexer extends JSONLexer {
    public JQAssistantJSONLexer(CharStream input, String pathOfInput) {
        super(input);

        removeErrorListeners();
        addErrorListener(new JSONFileScannerPlugin.MyErrorListener(pathOfInput));
    }
}
