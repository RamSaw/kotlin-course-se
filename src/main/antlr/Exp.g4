grammar Exp;

file
    : block EOF
    ;

block
    : (statement)*
    ;

blockWithBraces
    : '{' block '}'
    ;

statement
    : functionDefinition | variable | expression | whileStatement | ifStatement | assignment | returnStatement
    ;

functionDefinition
    : 'fun' IDENTIFIER '(' parameterNames ')' blockWithBraces
    ;

variable
    : 'var' IDENTIFIER ('=' expression)?
    ;

parameterNames
    : (IDENTIFIER (',' IDENTIFIER)*)?
    ;

whileStatement
    : 'while' '(' expression ')' blockWithBraces
    ;

ifStatement
    : 'if' '(' expression ')' blockWithBraces ('else' blockWithBraces)?
    ;

assignment
    : IDENTIFIER '=' expression
    ;

returnStatement
    : 'return' expression
    ;

expression
    : functionCall #functionCallExpression
    |
    leftOperand = expression
    operation = ('+' | '-' | '*' | '/' | '%' | '>' | '<' | '>=' | '<=' | '==' | '!=' | '||' | '&&')
    rightOperand = expression #binaryExpression
    | IDENTIFIER #identifierExpression
    | LITERAL #literalExpression
    | '(' expression ')' #expressionInBraces
    ;

functionCall
    : IDENTIFIER '(' arguments ')'
    ;

arguments
    : (expression (',' expression)*)?
    ;

IDENTIFIER
    : (LETTER | '_') (LETTER | '_' | DIGIT)*
    ;

LITERAL
    : '0' | '-'? [1-9] DIGIT*
    ;

fragment LETTER
    : [a-zA-Z]
    ;

fragment DIGIT
    : [0-9]
    ;

WS : (' ' | '\t' | '\r'| '\n' | '//' .*? (('\r')?'\n' | EOF)) -> skip;