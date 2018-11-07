package com.buschmais.jqassistant.plugin.json.impl.parsing;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONFileScannerPlugin.MyErrorListener;

import org.antlr.v4.runtime.CharStream;

/**
 * Lexer for JSON documents based on the generated JSON lexer by
 * ANTLR with configured listeners.
 */
public class JQAssistantJSONLexer extends JSONLexer {
    public JQAssistantJSONLexer(CharStream input, String pathOfInput) {
        super(input);

        removeErrorListeners();
        addErrorListener(new MyErrorListener(pathOfInput));
    }
}
