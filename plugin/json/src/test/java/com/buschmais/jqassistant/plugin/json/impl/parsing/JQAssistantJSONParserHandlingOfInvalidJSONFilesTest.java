package com.buschmais.jqassistant.plugin.json.impl.parsing;

import java.io.InputStream;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONFileScannerPlugin;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JQAssistantJSONParserHandlingOfInvalidJSONFilesTest {

    public static Stream<String> data() {
        return DataProvider.invalidOwnExamples();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void parserRecognizesAInvalidJSONFile(String pathToJSONFile) {
        IsNPECausedByANTLRIssue746Predicate antlrPredicate = new IsNPECausedByANTLRIssue746Predicate();
        ThrowableAssert.ThrowingCallable shouldRaiseThrowable = () -> {
            try (InputStream inputStream = getClass().getResourceAsStream(pathToJSONFile)) {

                JSONLexer l = new JQAssistantJSONLexer(CharStreams.fromStream(inputStream), pathToJSONFile);
                JSONParser p = new JQAssistantJSONParser(new CommonTokenStream(l), pathToJSONFile);

                p.document();
            }
        };

        assertThatThrownBy(shouldRaiseThrowable)
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



