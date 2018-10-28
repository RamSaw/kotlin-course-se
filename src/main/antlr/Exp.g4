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
    : functionDefinition | variableDeclaration | expression | whileStatement | ifStatement | assignment | returnStatement
    ;

functionDefinition
    : 'fun' IDENTIFIER '(' parameterNames ')' blockWithBraces
    ;

variableDeclaration
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
    : logicalOr
    ;

logicalOr
    : leftOperand=logicalAnd (operator='||' rightOperand=logicalOr)?
    ;

logicalAnd
    : leftOperand=equality (operator='&&' rightOperand=logicalAnd)?
    ;

equality
    : leftOperand=relational (operator=('==' | '!=') rightOperand=equality)?
    ;

relational
    : leftOperand=additive (operator=('>=' | '<=' | '>' | '<') rightOperand=relational)?
    ;

additive
    : leftOperand=multiplicative (operator=('+' | '-') rightOperand=additive)?
    ;

multiplicative
    : leftOperand=atom (operator=('*' | '/' | '%') rightOperand=multiplicative)?
    ;

atom
    : functionCall #functionCallExpression
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