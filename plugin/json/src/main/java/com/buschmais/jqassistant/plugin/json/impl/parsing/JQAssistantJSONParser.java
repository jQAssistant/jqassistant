package com.buschmais.jqassistant.plugin.json.impl.parsing;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONFileScannerPlugin.MyErrorListener;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;

/**
 * Parser for JSON documents based on the generated JSON parser by
 * ANTLR with configured listeners.
 */
public class JQAssistantJSONParser extends JSONParser {
    public JQAssistantJSONParser(TokenStream input, String pathOfInput) {
        super(input);

        removeErrorListeners();
        removeParseListeners();
        addErrorListener(new MyErrorListener(pathOfInput));
        addParseListener(new JSONNestingListener());

        setErrorHandler(new DefaultErrorStrategy() {
            @Override
            public void recover(Parser recognizer, RecognitionException e) {
                /*
                 * We don't recover from parsing errors as jQAssistant
                 * will work only on valid JSON documents. Therefore every
                 * error will stop the parsing of the underlying document
                 * and will be propagated.
                 * Oliver B. Fischer, 2017-04-16
                 */
                assert null != e : "Implementation assues that there is always an RecognationException";

                throw e;
            }
        });
    }
}
