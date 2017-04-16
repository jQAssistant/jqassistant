package com.buschmais.jqassistant.plugin.json.parser;

import com.buschmais.jqassistant.plugin.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parser.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONNestingListener;
import org.antlr.v4.runtime.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class JSONParsingTestSuiteIT {

    private DataProvider.T input;

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws URISyntaxException {
        return DataProvider.jsonParsingTestSuiteWithMetaData();
    }

    public JSONParsingTestSuiteIT(DataProvider.T i) {
        input = i;
    }

    @Test
    public void canHandleTheJSONParsingTestSuite() throws Exception {
        boolean passed = true;

        System.out.println(input.getFile().getName());
        try (InputStream inputStream = Files.newInputStream(input.getFile().toPath())) {
            BaseErrorListener errorListener = new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                        int charPos, String msg, RecognitionException e) {
                    String message = String.format("Failed to parse %s at %d:%d. Parser failed with: %s",
                                                   input.getFile().getName(), line, charPos, msg);
                    throw new IllegalStateException(message);
                }
            };

            JSONLexer l = new JSONLexer(CharStreams.fromStream(inputStream));
            JSONParser p = new JSONParser(new CommonTokenStream(l));
            p.removeErrorListeners();
            p.addErrorListener(errorListener);
            p.addParseListener(new JSONNestingListener());
            l.removeErrorListeners();
            l.addErrorListener(errorListener);

            p.document();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            passed = false;
        }

        if (input.isAcceptable()) {
            assertThat("File " + input.getFile().getName() + " should be accepted.", passed, is(true));
        } else {
            assertThat("File " + input.getFile().getName() + " should not be accepted.", passed, is(false));
        }
    }
 }
