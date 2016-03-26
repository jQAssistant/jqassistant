package org.jqassistant.plugin.json.parser;

import com.buschmais.jqassistant.plugins.json.impl.parser.JSONLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.buschmais.jqassistant.plugins.json.impl.parser.JSONLexer.T__4;
import static com.buschmais.jqassistant.plugins.json.impl.parser.JSONLexer.T__5;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@RunWith(Parameterized.class)
public class JSONLexerTest {

    @Parameter(0)
    public String input;

    @Parameter(1)
    public String[] expectedTokens;

    @Parameter(2)
    public Integer[] exptectedTypeIds;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
             {"[]", new String[]{"[", "]"}, new Integer[] {T__4, T__5}},
             {"[\"VALUE\"]", new String[]{"[", "VALUE", "]"}, new Integer[]{T__4, JSONLexer.STRING, T__5}}
        });
    }

    @Test
    public void lexerOuput() throws Exception {
//        String textInput = "[]";
        JSONLexer lexer = new JSONLexer(new ANTLRInputStream(input));

//        String[] expectedTokens = new String[]{"[", "]"};
        List<? extends Token> foundTokens = lexer.getAllTokens();

        assertThat("Number of expected and found tokens must be the same.",
                   foundTokens.size(), Matchers.is(expectedTokens.length));

        for (int i = 0; i < expectedTokens.length; i++) {
            assertThat("Expected token and found token text mismatch.",
                       foundTokens.get(i).getText(), equalTo(expectedTokens[i]));

            assertThat(foundTokens.get(i).getType(), equalTo(exptectedTypeIds[i]));
        }
    }
}
