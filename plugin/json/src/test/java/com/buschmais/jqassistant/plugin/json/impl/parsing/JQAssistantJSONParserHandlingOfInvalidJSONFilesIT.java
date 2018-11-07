package com.buschmais.jqassistant.plugin.json.impl.parsing;

import java.io.InputStream;
import java.util.Collection;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONFileScannerPlugin;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class JQAssistantJSONParserHandlingOfInvalidJSONFilesIT {

    private String pathToJSONFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return DataProvider.invalidOwnExamples();
    }

    public JQAssistantJSONParserHandlingOfInvalidJSONFilesIT(String path) {
        pathToJSONFile = path;
    }

    @Test
    public void parserRecognizesAInvalidJSONFile() {
        IsNPECausedByANTLRIssue746Predicate antlrPredicate = new IsNPECausedByANTLRIssue746Predicate();
        ThrowableAssert.ThrowingCallable shouldRaiseThrowable = new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                try (InputStream inputStream = getClass().getResourceAsStream(pathToJSONFile)) {

                    JSONLexer l = new JQAssistantJSONLexer(CharStreams.fromStream(inputStream), pathToJSONFile);
                    JSONParser p = new JQAssistantJSONParser(new CommonTokenStream(l), pathToJSONFile);

                    p.document();
                }
            }
        };

        Assertions.assertThatThrownBy(shouldRaiseThrowable)
                  .isInstanceOfAny(IllegalStateException.class,
                                   RecognitionException.class,
                                   JSONFileScannerPlugin.RecoverableParsingException.class,
                                   NullPointerException.class)
                  .satisfies(exception -> {
                      if (NullPointerException.class.isAssignableFrom(exception.getClass())) {
                          boolean predicateResult = antlrPredicate.isNPECausedByANTLRIssue746Predicate(exception);

                          assertThat(predicateResult).isTrue();
                      }
                  });
    }
}



