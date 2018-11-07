package com.buschmais.jqassistant.plugin.json.parser;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collection;

import com.buschmais.jqassistant.plugin.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parser.JSONParser;
import com.buschmais.jqassistant.plugin.json.impl.scanner.IsNPECausedByANTLRIssue746Predicate;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JQAssistantJSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.scanner.JQAssistantJSONParser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JQAssistantJSONParserForValidJSONFilesOfTestSuiteIT {

    private File jsonFile;

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws URISyntaxException {
        return DataProvider.validFilesOfJSONParsingTestSuite();
    }

    public JQAssistantJSONParserForValidJSONFilesOfTestSuiteIT(File file) {
        jsonFile = file;
    }

    @Test
    public void canParseAValidJSONFileOfTheTestSuite() throws Exception {
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
