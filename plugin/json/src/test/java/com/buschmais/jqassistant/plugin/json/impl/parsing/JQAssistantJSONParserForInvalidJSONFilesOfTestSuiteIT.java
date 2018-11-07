package com.buschmais.jqassistant.plugin.json.impl.parsing;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JSONFileScannerPlugin;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.NoViableAltException;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JQAssistantJSONParserForInvalidJSONFilesOfTestSuiteIT extends AbstractPluginIT {
    private IsNPECausedByANTLRIssue746Predicate antlrPredicate = new IsNPECausedByANTLRIssue746Predicate();
    private File jsonFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        return DataProvider.invalidFilesOfJSONParsingTestSuite();
    }

    @Before
    public void startTransaction() {
        store.beginTransaction();
    }

    @After
    public void commitTransaction() {
        store.commitTransaction();
    }

    public JQAssistantJSONParserForInvalidJSONFilesOfTestSuiteIT(File file) {
        jsonFile = file;
    }

    @Test
    public void failsOnParsingAnInvalidJSONFileOfTheTestSuite() {
        class ANTLRRecognisedErrorNotFailedToReportItProperly extends Exception {
        }

        Assertions.assertThatThrownBy(() -> {
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
