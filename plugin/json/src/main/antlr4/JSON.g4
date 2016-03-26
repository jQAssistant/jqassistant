//
// Grammar build build on the grammar on http://json.org and
// the grammar at https://github.com/antlr/grammars-v4/tree/master/json
//

grammar JSON;

@header {
    package com.buschmais.jqassistant.plugins.json.impl.parser;
}


jsonDocument
    :   jsonObject
    |   jsonArray
    ;


jsonObject
    :   '{' keyValuePair (',' keyValuePair)* '}'
    |   '{' '}'
    |
    ;

keyValuePair
    :   STRING ':' value
    ;

arrayElements
    :   value (',' value)*
    ;

jsonArray
    :   '[' ']'
    |   '[' arrayElements ']'
    |
    ;

value
    :   jsonArrayValue
    |   jsonObjectValue
    |   jsonScalarValue
    ;

jsonArrayValue
    :   jsonArray
    ;

jsonObjectValue
    :   jsonObject
    ;

jsonScalarValue
    :
    |   STRING
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
    :   'u' HEX HEX HEX HEX
    ;

fragment ESC
    :   '\\' (["\\/bfnrt] | UNICODE_ESCAPE_SEQ)
    ;

NULL
    :   'null'
    ;

BOOLEAN
    :   ('true'|'false')
    ;


STRING
    :   '"' (ESC | ~["\\])* '"'
        // Solution taken from https://theantlrguy.atlassian.net/wiki/x/HgAp
        // See also http://stackoverflow.com/questions/33281312/
        { setText(getText().substring(1, getText().length() - 1)); }
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






