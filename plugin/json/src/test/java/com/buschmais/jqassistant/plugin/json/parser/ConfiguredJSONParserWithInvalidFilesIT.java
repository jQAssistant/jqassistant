package com.buschmais.jqassistant.plugin.json.parser;

import com.buschmais.jqassistant.plugin.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parser.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.ConfiguredJSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.scanner.ConfiguredJSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.IsNPECausedByANTLRIssue746Predicate;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONFileScannerPlugin;
import org.antlr.v4.runtime.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.InputStream;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ConfiguredJSONParserWithInvalidFilesIT {

    private String pathToJSONFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return DataProvider.invalidOwnExamples();
    }

    public ConfiguredJSONParserWithInvalidFilesIT(String path) {
        pathToJSONFile = path;
    }

    @Test
    public void parserRecognizesAInvalidJSONFile() throws Exception {
        IsNPECausedByANTLRIssue746Predicate antlrPredicate = new IsNPECausedByANTLRIssue746Predicate();
        ThrowableAssert.ThrowingCallable shouldRaiseThrowable = new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                try (InputStream inputStream = getClass().getResourceAsStream(pathToJSONFile)) {

                    JSONLexer l = new ConfiguredJSONLexer(CharStreams.fromStream(inputStream), pathToJSONFile);
                    JSONParser p = new ConfiguredJSONParser(new CommonTokenStream(l), pathToJSONFile);

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



