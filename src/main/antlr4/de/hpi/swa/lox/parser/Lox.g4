grammar Lox; 

// Lox Grammar adapted from https://craftinginterpreters.com/appendix-i.html

@parser::header
{
// DO NOT MODIFY - generated from Lox.g4
}

@lexer::header
{
// DO NOT MODIFY - generated from Lox.g4
}

program        : declaration* EOF ;

declaration    :  classDecl 
               | funDecl
               | varDecl
               | statement ;

classDecl      : 'class' name=IDENTIFIER ('<' extends=IDENTIFIER )?
                 '{' function* '}' ;

statement      : exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | returnStmt
               | whileStmt
               | haltStmt
               | hackStmt
               | block;

funDecl        : 'fun' function ';'? ;
varDecl        : 'var' IDENTIFIER ('=' expression )? ';' ;

exprStmt       : expression ';' ;
forStmt        : 'for' '(' (varDecl | exprStmt | ';' )
                           condition=expression? ';'
                           increment=expression? ')' body=statement ;
ifStmt         : 'if' '(' condition=expression ')' then=statement
                 ( 'else' alt=statement )? ;
printStmt      : 'print' expression ';' ;
returnStmt     : 'return' expression? ';' ;
whileStmt      : 'while' '(' condition=expression ')' body=statement;

haltStmt       : 'halt;' ;
hackStmt       : 'hack;' ; // for debugging

block          : '{' declaration* '}' ;

expression     : assignment ;

assignment     : ( left=arrayExpr '.' )? IDENTIFIER '=' assignment
               | arrayAssignment ;

arrayAssignment    : left=arrayExpr '[' index=expression ']' '=' right=assignment
               | other=logic_or;

logic_or       : logic_and ( 'or' logic_and )* ;

// logic_and      : equality ( 'and' equality )* ;

logic_and      : bit_or ( 'and' bit_or )* ;
bit_or       : bit_and ( '|' bit_and )* ;
bit_and       : equality ( '&' equality )* ;
equality       : comparison ( ( '!=' | '==' ) comparison )* ;
comparison     : term ( ( '>' | '>=' | '<' | '<=' ) term )* ;
term           : factor ( ( '-' | '+' ) factor )* ;
factor         : remainder ( ( '/' | '*' ) remainder )* ;

remainder         : unary ( '%'  unary )* ;

unary          : ( '!' | '-' ) unary | arrayExpr;

arrayExpr       : left=call ('[' expression ']')*;

call           : primary callArguments* ;

callArguments : '(' arguments? ')' | '.' IDENTIFIER;

primary         : number | string | true | false | nil | array |  superExpr | variableExpr | '(' expression ')' ;

superExpr   :   'super' '.' IDENTIFIER;

variableExpr    : IDENTIFIER;

array : '[]';

function       : IDENTIFIER '(' parameters? ')' block ;
parameters     : IDENTIFIER ( ',' IDENTIFIER )* ;
arguments      : expression ( ',' expression )* ;

string          : STRING ;
nil             : 'nil';
true            : 'true';
false           : 'false';
number          : NUMBER;

NUMBER          : DIGIT+ ( '.' DIGIT+ )? ;
STRING          : '"' (~["\\])* '"' ;
IDENTIFIER      : ALPHA ( ALPHA | DIGIT )* ;
fragment ALPHA  : [a-zA-Z_] ;
fragment DIGIT  : [0-9] ;

// more... 
WS             : [ \t\r\n]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;


// Local Variables:
// eval: (add-hook 'after-save-hook (lambda () (if (fboundp 'lsp-workspace-root) (if-let ((workspace (car (gethash (lsp-workspace-root) (lsp-session-folder->servers (lsp-session)))))) (with-lsp-workspace workspace (lsp-notify "workspace/didChangeWatchedFiles" `((changes . [((type . ,(alist-get 'changed lsp--file-change-type)) (uri . ,(lsp--path-to-uri buffer-file-name)))]))))))) nil t)
// End:
