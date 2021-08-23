package com.buschmais.jqassistant.plugin.json.impl.parsing;

import java.io.InputStream;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONParser;

import org.antlr.v4.runtime.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class JQAssistantJSONParserHandlingOfValidJSONFilesTest {

    static Stream<String> data() {
        return DataProvider.validOwnExamples();
    }

    @MethodSource("data")
    @ParameterizedTest
    void canParseValidJSONFile(String pathToJSONFile) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream(pathToJSONFile)) {
            JSONLexer l = new JQAssistantJSONLexer(CharStreams.fromStream(inputStream), pathToJSONFile);
            JSONParser p = new JQAssistantJSONParser(new CommonTokenStream(l), pathToJSONFile);
            p.document();
        }
    }

 }
