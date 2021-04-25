/* 
 * Grammar for the Wool-W compiler, CS4533 and CS544, Worcester Polytechnic Institute
 * Author: Gary F. Pollice
 */
grammar Wool;

@header {
package wool.lexparse;
}

// Parser rules
program: classes+=classDef+ EOF;       // Non-greedy just to not have a warning
        
classDef: 'class' className=TYPE ('inherits' inherits=TYPE)*? classBody;        
                
classBody: OPEN_BRACKET (variables+=variableDef | methods+=method)* CLOSE_BRACKET;

method: methodName=ID '(' (formals+=formal (',' formals+=formal)*)? ')' ':' type=typeName  OPEN_BRACKET vars+=variableDef* expr CLOSE_BRACKET ;
	
variableDef: name=ID ':' type=typeName ('<-' initializer=expr)? EL;

formal: name=ID ':' type=typeName;

typeName: classType=TYPE | intType = 'int' | boolType = 'boolean';	
	
expr: object=expr '.' methodName=ID '(' (args+=expr (',' args+=expr)*)? ')' #FullMethodCall
	|  methodName=ID '(' (args+=expr (',' args+=expr)*)? ')' #LocalMethodCall
	| 'if' condition=expr 'then' thenExpr=expr 'else' elseExpr=expr 'fi' #IfExpr
	| 'while' condition=expr 'loop' loopExpr=expr 'pool' #WhileExpr
	| '{' (exprs+=expr EL)+ '}' #ExprList
	| 'select' selectAlt+  'end' #SelectExpr
	| 'new' type=TYPE #NewExpr
	| <assoc=right> '-' expr #UMinusExpr
	| 'isnull' expr #IsNullExpr
	| left=expr ('*' | '/') right=expr #MultExpr
	| left=expr ('+' | '-') right=expr #AddExpr
	| left=expr ('<' | '<=' | '>' | '>=') right=expr #RelExpr
	| <assoc=right> left=expr ('=' | '~=') right=expr # EqExpr
    | <assoc=right> '~' expr # NotExpr
	| '(' expr ')' #ParenExpr
	| ID '<-' expr #AssignExpr
	| ID #IDExpr
	| NUM #NumExpr
	| STRING #StrExpr
	| 'true' #TrueExpr
	| 'false' #FalseExpr
	| 'null' #NullExpr
	;

selectAlt :   expr ':' expr ';' ;

NUM: [0-9]+; 
TYPE: ([A-Z][a-zA-Z0-9_]*);            
ID: [a-z] [a-zA-Z_0-9]*; //should be the last rule

EL: ';';
OPEN_BRACKET: '{';
CLOSE_BRACKET: '}';
	
COMMENT: ( ('(*' .*? '*)') | ('#' .*? '\n')) -> skip;
STRING:  '"' ('\\'. | ~[\n])*? '"';
FORGET: [ \t\r\n\f]+ -> skip ;   

fragment ESC: '\\"' | '\\\\' | '\\t' | '\\b' | '\\n' | '\\r' | '\\f' | '\\';