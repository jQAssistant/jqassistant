package com.buschmais.jqassistant.plugin.json.parser;

import com.buschmais.jqassistant.plugin.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parser.JSONParser;
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
public class JSONParserWithInvalidFilesIT {

    private String pathToJSONFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
             {"/probes/invalid/empty-file.json"}
        });
    }

    public JSONParserWithInvalidFilesIT(String path) {
        pathToJSONFile = path;
    }

    @Test(expected = IllegalStateException.class)
    public void parserRecognizesAInvalidJSONFile() throws Exception {

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

        p.document();
    }

 }
