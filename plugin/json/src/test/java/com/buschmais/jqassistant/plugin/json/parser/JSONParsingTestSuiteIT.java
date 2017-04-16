package com.buschmais.jqassistant.plugin.json.parser;

import com.buschmais.jqassistant.plugin.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parser.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.*;
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
        boolean passed = false;

        try (InputStream inputStream = Files.newInputStream(input.getFile().toPath())) {
            JSONLexer l = new ConfiguredJSONLexer(CharStreams.fromStream(inputStream), "/not/given");
            JSONParser p = new ConfiguredJSONParser(new CommonTokenStream(l), "/not/given");

            p.document();
            passed = true;
        } catch (JSONFileScannerPlugin.RecoverableParsingException | RecognitionException | IllegalStateException  e) {

            passed = false;
        } catch (NullPointerException t) {
            boolean isFromANTLR = new IsNPECausedByANTLRIssue746Predicate().isNPECausedByANTLRIssue746Predicate(t);

            if (!isFromANTLR) {
                throw t;
            }
        }

        if (input.isAcceptable()) {
            assertThat("File " + input.getFile().getName() + " should be accepted.", passed, is(true));
        } else {
            assertThat("File " + input.getFile().getName() + " should not be accepted.", passed, is(false));
        }
    }
 }
