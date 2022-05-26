lexer grammar MyxalLexer;

PREFIX
    : [¨Þø∆k]
    ;

CONTEXT_VAR
    : 'n'
    ;

ALIAS
    : '¢'
    ;

MODIFIER
    : 'ß'
    | 'v'
    | 'ƒ'
    | 'ɖ'
    | '⁺'
    | '₌'
    | '₍'
    | '~'
    | '&'
    | '¨='
    ;

COMMENT
    : '#' (~'\n' .)* -> skip
    ;

DIGIT
    : [0-9]
    ;

MINUS
    : '-'
    ;

ALPHA
    : [A-Za-z]
    ;

WHITESPACE
    : [ \t\r\n]
    ;

ASSN_SIGN
    : '→' | '←'
    ;

LAMBDA_TYPE
    : [λƛ'µ]
    ;

NORMAL_STRING
    : '"' .*? '"'
    ;

COMPRESSED_STRING
    : '«' .*? '«'
    ;

SINGLE_CHAR_STRING
    : '\\' .
    ;

DOUBLE_CHAR_STRING
    : '‛' . .
    ;

// syntax elements
PIPE
    : '|'
    ;

WHILE_OPEN
    : '{'
    ;

WHILE_CLOSE
    : '}'
    ;

IF_OPEN
    : '['
    ;

IF_CLOSE
    : ']'
    ;

FOR_OPEN
    : '('
    ;

FOR_CLOSE
    : ')'
    ;

LIST_OPEN
    : '⟨'
    ;

LIST_CLOSE
    : '⟩'
    ;

PERIOD
    : '.'
    ;

SEMICOLON
    : ';'
    ;

AT_SIGN
    : '@'
    ;

STAR
    : '*'
    ;

COLON
    : ':'
    ;

COMPRESSED_NUMBER
    : '»'
    ;

COMPLEX_SEPARATOR
    : '°'
    ;

ONE_ELEMENT_LAMBDA
    : '⁽'
    ;

TWO_ELEMENT_LAMBDA
    : '‡'
    ;

THREE_ELEMENT_LAMBDA
    : '≬'
    ;

LITERALLY_ANY_TEXT
    : [\u0010-\uFFFF]
    ;
