package com.buschmais.jqassistant.plugin.json.impl.parsing;

import java.util.List;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer.STRING;
import static com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer.T__4;
import static com.buschmais.jqassistant.plugin.json.impl.parsing.generated.JSONLexer.T__5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class JQAssistantJSONLexerTest {
    public static Stream<Arguments> data() {
        return Stream.of(arguments("[]", new String[]{"[", "]"}, new Integer[] {T__4, T__5}),
                         arguments("[\"VALUE\"]", new String[]{"[", "VALUE", "]"}, new Integer[]{T__4, STRING, T__5}));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void lexerOutput(String input, String[] expectedTokens, Integer[] exptectedTypeIds) {
        JSONLexer lexer = new JQAssistantJSONLexer(CharStreams.fromString(input), "/not/given");

        List<? extends Token> foundTokens = lexer.getAllTokens();

        assertThat(foundTokens).as("Number of expected and found tokens must be the same.")
                               .hasSize(expectedTokens.length);

        for (int i = 0; i < expectedTokens.length; i++) {
            assertThat(foundTokens.get(i).getText()).as("Expected token and found token text mismatch.")
                                                    .isEqualTo(expectedTokens[i]);

            assertThat(foundTokens.get(i).getType()).isEqualTo(exptectedTypeIds[i]);
        }
    }
}
