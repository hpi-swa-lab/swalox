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

declaration    :  statement ;

statement      : printStmt;

printStmt      : 'print' expression ';' ;

expression     : true;

true           : 'true';

// more... 
WS             : [ \t\r\n]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;

// Local Variables:
// eval: (add-hook 'after-save-hook (lambda () (if (fboundp 'lsp-workspace-root) (if-let ((workspace (car (gethash (lsp-workspace-root) (lsp-session-folder->servers (lsp-session)))))) (with-lsp-workspace workspace (lsp-notify "workspace/didChangeWatchedFiles" `((changes . [((type . ,(alist-get 'changed lsp--file-change-type)) (uri . ,(lsp--path-to-uri buffer-file-name)))]))))))) nil t)
// End:
