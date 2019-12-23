package com.buschmais.jqassistant.plugin.json.impl.parsing;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONFileScannerPlugin;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.NoViableAltException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JQAssistantJSONParserForInvalidJSONFilesOfTestSuiteTest {
    private IsNPECausedByANTLRIssue746Predicate antlrPredicate = new IsNPECausedByANTLRIssue746Predicate();

    public static Stream<File> data() throws Exception {
        return DataProvider.invalidFilesOfJSONParsingTestSuite();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void failsOnParsingAnInvalidJSONFileOfTheTestSuite(File jsonFile) {
        class ANTLRRecognisedErrorNotFailedToReportItProperly extends Exception {
        }

        assertThatThrownBy(() -> {
            try (InputStream inputStream = Files.newInputStream(jsonFile.toPath())) {
                JSONLexer l = new JQAssistantJSONLexer(CharStreams.fromStream(inputStream), "/not/given");
                JSONParser p = new JQAssistantJSONParser(new CommonTokenStream(l), "/not/given");

                try {
                    p.document();
                } catch (NullPointerException npe) {
                    if (antlrPredicate.isNPECausedByANTLRIssue746Predicate(npe)) {
                        throw new ANTLRRecognisedErrorNotFailedToReportItProperly();
                    }
                }
            }
        }).isInstanceOfAny(LexerNoViableAltException.class,
                           IllegalStateException.class,
                           NoViableAltException.class,
                           ANTLRRecognisedErrorNotFailedToReportItProperly.class,
                           JSONFileScannerPlugin.RecoverableParsingException.class,
                           InputMismatchException.class);
    }
}
