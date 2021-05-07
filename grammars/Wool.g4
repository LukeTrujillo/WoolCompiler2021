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
	
variableDef: dec=formal ('<-' initializer=expr)? EL;

formal: name=ID ':' type=typeName;

typeName: classType=TYPE | intType = 'int' | boolType = 'boolean';	
	
expr: object=expr '.' methodName=ID '(' (args+=expr (',' args+=expr)*)? ')' #FullMethodCall
	|  methodName=ID '(' (args+=expr (',' args+=expr)*)? ')' #LocalMethodCall
	| 'if' condition=expr 'then' thenExpr=expr 'else' elseExpr=expr 'fi' #IfExpr
	| 'while' condition=expr 'loop' loopExpr=expr 'pool' #WhileExpr
	| '{' (exprs+=expr EL)+ '}' #ExprList
	| 'select' selectAlt+  'end' #SelectExpr
	| 'new' type=TYPE #NewExpr
	| <assoc=right> op='-' right=expr #UMinusExpr
	| 'isnull' expr #IsNullExpr
	| left=expr op=('*' | '/' | '+' | '-') right=expr #MathExpr
	| left=expr op=('<' | '<=' | '>' | '>=') right=expr #RelExpr
	| <assoc=right> left=expr op=('=' | '~=') right=expr # EqExpr
    | <assoc=right> op='~' left=expr # NotExpr
	| '(' go=expr ')' #ParenExpr
	| variableName=ID '<-' expr #AssignExpr
	| name=ID #IDExpr
	| niceNumber=NUM #NumExpr
	| s=STRING #StrExpr
	| t='true' #TrueExpr
	| f='false' #FalseExpr
	| n='null' #NullExpr
	;

selectAlt :   expr ':' expr ';' ;

STRING:  '"' ('\\'. | ~[\n])*? '"';

NUM: [0-9]+; 
TYPE: ([A-Z][a-zA-Z0-9_]*);            
ID: [a-z] [a-zA-Z_0-9]*; //should be the last rule

EL: ';';
OPEN_BRACKET: '{';
CLOSE_BRACKET: '}';
	
COMMENT: ( ('(*' .*? '*)') | ('#' .*? '\n')) -> skip;
FORGET: [ \t\r\n\f]+ -> skip ;   

fragment ESC: '\\"' | '\\\\' | '\\t' | '\\b' | '\\n' | '\\r' | '\\f' | '\\';