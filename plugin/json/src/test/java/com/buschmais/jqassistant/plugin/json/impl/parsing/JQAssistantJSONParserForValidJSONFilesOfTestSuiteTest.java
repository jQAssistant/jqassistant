package com.buschmais.jqassistant.plugin.json.impl.parsing;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class JQAssistantJSONParserForValidJSONFilesOfTestSuiteTest {

    static Stream<File> data() throws URISyntaxException {
        return DataProvider.validFilesOfJSONParsingTestSuite();
    }

    @ParameterizedTest
    @MethodSource("data")
    void canParseAValidJSONFileOfTheTestSuite(File jsonFile) throws Exception {
        try (InputStream inputStream = Files.newInputStream(jsonFile.toPath())) {
            JSONLexer l = new JQAssistantJSONLexer(CharStreams.fromStream(inputStream), "/not/given");
            JSONParser p = new JQAssistantJSONParser(new CommonTokenStream(l), "/not/given");

            p.document();
        } catch (NullPointerException t) {
            boolean isFromANTLR = new IsNPECausedByANTLRIssue746Predicate().isNPECausedByANTLRIssue746Predicate(t);

            if (!isFromANTLR) {
                throw t;
            }
        }
    }
 }
