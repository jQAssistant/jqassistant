//
// Grammar build build on grammar provided in RFC 7159
// and based on the grammar at https://github.com/antlr/grammars-v4/tree/master/json
//

grammar JSON;

@header {
    package com.buschmais.jqassistant.plugin.json.impl.parsing.generated;

    import java.util.regex.*;
}


@lexer::members {
    private static Pattern ESCAPE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{2})([0-9a-fA-F]{2})");
}

@parser::members {
}


document
    :   object EOF
    |   array  EOF
    |   scalarValue EOF
    ;

object
    :   '{' keyValuePair (',' keyValuePair)* '}'
    |   '{' '}'
    ;

keyValuePair
    :   STRING ':' value
    ;

arrayElements
    :   value (',' value)*
    ;

array
    :   '[' ']'
    |   '[' arrayElements ']'
    ;

value
    :   array
    |   object
    |   scalarValue
    ;

scalarValue
    :   STRING
    |   NUMBER
    |   BOOLEAN
    |   NULL
    ;

fragment E
    :   [Ee] [+\-]? INT
    ;

fragment INT
    :   '0' | [1-9] [0-9]*
    ;

fragment HEX
    :   [0-9a-fA-F]
    ;

fragment UNICODE_ESCAPE_SEQ
    :   '\\' 'u' HEX HEX HEX HEX
    ;

fragment ESCAPE_SEQUENCE
    :    '\\' ["\\/bfnrt]
    ;

fragment ESC
    :   ESCAPE_SEQUENCE | UNICODE_ESCAPE_SEQ
    ;

NULL
    :   'null'
    ;

BOOLEAN
    :   ('true'|'false')
    ;


// See section 7. Strings of RFC 7159
STRING
     :   '"' (ESC | ~(["\\] | '\u0000' .. '\u001f'))* '"'
        // Solution taken from https://theantlrguy.atlassian.net/wiki/x/HgAp
        // See also http://stackoverflow.com/questions/33281312/
        // See also http://stackoverflow.com/questions/39398698/

        // Source for possible optimization https://github.com/antlr/antlr4/issues/477
        {
            StringBuilder result = new StringBuilder();
            int startOfNormalText = 0;
            int endOfNormalText = 0;

            int startOfMatch = 0;
            int endOfMatch = 0;

            String text = getText().substring(1, getText().length() - 1);

            Matcher matcher = ESCAPE_PATTERN.matcher(text);

            if (!matcher.find()) {
                result.append(text);
            } else {
                do {
                    startOfMatch = matcher.start();
                    endOfMatch = matcher.end();

                    {
                        endOfNormalText = startOfMatch;

                        String normalText = text.substring(startOfNormalText, endOfNormalText);
                    }

                    String m = text.substring(startOfMatch, endOfMatch);
                    endOfNormalText = startOfNormalText = endOfMatch;
                    String hexPart = m.split("u")[1];

                    result.append((char) Integer.parseInt(hexPart, 16));
                } while (matcher.find());

                if (endOfMatch != text.length() - 1) {
                    result.append(text.substring(endOfMatch));
                }
            }

            String stringValue = result.toString();

            stringValue = stringValue.replaceAll("\\\\\"", "\"");
            stringValue = stringValue.replaceAll("\\\\f", "\f");
            stringValue = stringValue.replaceAll("\\\\b", "\b");
            stringValue = stringValue.replaceAll("\\\\n", "\n");
            stringValue = stringValue.replaceAll("\\\\r", "\r");
            stringValue = stringValue.replaceAll("\\\\t", "\t");

            setText(stringValue);
        }
    ;

WHITESPACE
    :   [ \t\n\r]+ -> skip
    ;

NUMBER
    :   '-'? INT '.' [0-9]+ E?
    |   '-'? INT E
    |   '-'? INT
    ;

// Some JSON parsers support line comments
LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;

// Some JSON parsers support block comments
BLOCK_COMMENT
    :   '/*' .*? '*/' -> skip
    ;






