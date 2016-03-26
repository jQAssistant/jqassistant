package org.jqassistant.jqassistant.plugin.json.parser;

import com.buschmais.jqassistant.plugins.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugins.json.impl.parser.JSONParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class JSONParserIT {

    private String pathToJSONFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
             {"/probes/valid/array-empty.json"},
             {"/probes/valid/array-one-value.json"},
             {"/probes/valid/line-comment-before-object.json"},
             {"/probes/valid/line-comment-in-object.json"},
             {"/probes/valid/line-comment-after-object.json"},
             {"/probes/valid/block-comment-in-object.json"},
             {"/probes/valid/true-false-null.json"},
             {"/probes/valid/empty-file.json"},
             {"/probes/valid/object-with-objects.json"},
             {"/probes/valid/object-one-key-value-pair.json"},
             {"/probes/valid/object-two-key-value-pairs.json"},
             {"/probes/valid/string-value-with-quote-mark.json"},
             {"/probes/valid/string-value-with-unicode-signs.json"},
             {"/probes/valid/object-with-array-empty.json"},
             {"/probes/valid/object-with-array.json"},
             {"/probes/valid/object-with-array-two-elements.json"},
             {"/probes/valid/object-with-number.json"}
        });
    }

    public JSONParserIT(String path) {
        pathToJSONFile = path;
    }

    @Test
    public void canParseValidJSONFile() throws Exception {

        InputStream inputStream = getClass().getResourceAsStream(pathToJSONFile);
        JSONLexer l = new JSONLexer(new ANTLRInputStream(inputStream));
        JSONParser p = new JSONParser(new CommonTokenStream(l));
        p.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPos, String msg, RecognitionException e) {
                String message = String.format("Failed to parse %s at %d:%d. Parser failed with: %s",
                                               pathToJSONFile, line, charPos, msg);
                throw new IllegalStateException(message);
            }
        });

        p.jsonObject();
    }

 }
