# Crafting interpreters written in Java

## Grammar definition
```
program     -> declaration* EOF ;
declaration -> classDecl | funDecl | varDecl | statement ;
classDecl   -> "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;
funDecl     -> "fun" function ;
function    -> IDENTIFIER "(" parameters? ")" block ;
parameters  -> IDENTIFIER ( "," IDENTIFIER )* ;
statement   -> exprStmt | forStmt | ifStmt | printStmt | returnStmt | whileStmt | block ;
returnStmt  -> "return" expression? ";" ;
ifStmt      -> "if" "(" expression ")" statment ( "else" statement )? ;
forStmt     -> "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;
exprStmt    -> expression ";" ;
printStmt   -> "print" expression ";" ;
whileStmt   -> "while" "(" expression ")" statement ;
block       -> "{" declaration* "}" ;
varDecl     -> "var" IDENTIFIER ( "=" expression )? ";" ;
expression  -> assignment ;
assignment  -> ( call "." )? IDENTIFIER "=" assignment | logic_or ;
logic_or    -> logic_and ( "or" logic_and )* ;
logic_and   -> equality ( "and" equality )* ;
unary       -> ( "!" | "-" ) unary | call ;
call        -> primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
primary     -> "true" | "false" | "nil" | "this" | NUMBER | STRING | IDENTIFIER | "(" expression ")" | "super" "." IDENTIFIER ;
```

## AST walker

## Virtual machine