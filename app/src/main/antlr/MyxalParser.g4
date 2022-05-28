parser grammar MyxalParser;

options {
    tokenVocab=MyxalLexer;
}

@members {
boolean isAlias = false;
}

file
    : {isAlias = true;} alias* {isAlias = false;} program EOF
    ;

alias
    : program ALIAS WHITESPACE* theAlias=element
    ;

program
    : (program_node | WHITESPACE)*
    ;

program_node
    : statement | literal | modifier | element
    ;

literal
    : string
    | number
    | compressed_number
    | complex_number
    | list
    ;

string
    : NORMAL_STRING
    | COMPRESSED_STRING
    | SINGLE_CHAR_STRING
    | DOUBLE_CHAR_STRING
    ;

number
    : MINUS? integer (PERIOD integer)?
    ;

integer
    : DIGIT+
    ;

compressed_number
    : COMPRESSED_NUMBER .*? COMPRESSED_NUMBER
    ;

complex_number
    : number COMPLEX_SEPARATOR number
    ;

list
    : LIST_OPEN program (PIPE program)* LIST_CLOSE?
    ;

statement
    : if_statement
    | fori_loop
    | for_loop
    | while_loop
    | lambda
    | one_element_lambda
    | two_element_lambda
    | three_element_lambda
    | variable_assn
    ;

if_statement
    : IF_OPEN program (PIPE program)? IF_CLOSE?
    ;

fori_loop
    : DIGIT DIGIT? DIGIT? DIGIT? DIGIT? DIGIT? DIGIT? DIGIT? DIGIT? FOR_OPEN program FOR_CLOSE?
    ;

for_loop
    : FOR_OPEN (variable PIPE)? program FOR_CLOSE?
    ;

while_loop
    : WHILE_OPEN (cond=program PIPE)? body=program WHILE_CLOSE?
    ;

lambda
    : LAMBDA_TYPE (integer PIPE)? program SEMICOLON?
    ;

one_element_lambda
    : ONE_ELEMENT_LAMBDA program_node
    ;

two_element_lambda
    : TWO_ELEMENT_LAMBDA program_node program_node
    ;

three_element_lambda
    : THREE_ELEMENT_LAMBDA program_node program_node program_node
    ;

variable_assn
    : ASSN_SIGN variable
    ;

variable
    : (ALPHA | DIGIT | CONTEXT_VAR)+
    ;

modifier
    : MODIFIER+ mod_node
    ;

mod_node
    : (element | statement | literal)
    ;

element locals [boolean isInAlias]
    : PREFIX? element_type {$isInAlias = isAlias;}
    ;

element_type
    : ALPHA | LITERALLY_ANY_TEXT | CONTEXT_VAR | DIGIT | MODIFIER | MINUS | STAR
    ;

