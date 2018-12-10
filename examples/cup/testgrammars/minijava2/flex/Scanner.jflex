package miniparser;

import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;

%%

%public
%class Lexer
%cup
%implements sym, minijava.Constants
%char
%line
%column

%{
    StringBuffer string = new StringBuffer();
    public Lexer(java.io.Reader in, ComplexSymbolFactory sf){
	this(in);
	symbolFactory = sf;
    }
    ComplexSymbolFactory symbolFactory;

  private Symbol symbol(String name, int sym) {
      return symbolFactory.newSymbol(name, sym, new Location(yyline+1,yycolumn+1,yychar), new Location(yyline+1,yycolumn+yylength(),yychar+yylength()));
  }
  
  private Symbol symbol(String name, int sym, Object val) {
      Location left = new Location(yyline+1,yycolumn+1,yychar);
      Location right= new Location(yyline+1,yycolumn+yylength(), yychar+yylength());
      return symbolFactory.newSymbol(name, sym, left, right,val);
  } 
  private Symbol symbol(String name, int sym, Object val,int buflength) {
      Location left = new Location(yyline+1,yycolumn+yylength()-buflength,yychar+yylength()-buflength);
      Location right= new Location(yyline+1,yycolumn+yylength(), yychar+yylength());
      return symbolFactory.newSymbol(name, sym, left, right,val);
  }       
  private void error(String message) {
    System.out.println("Error at line "+(yyline+1)+", column "+(yycolumn+1)+" : "+message);
  }
%} 

%eofval{
     return symbolFactory.newSymbol("EOF", EOF, new Location(yyline+1,yycolumn+1,yychar), new Location(yyline+1,yycolumn+1,yychar+1));
%eofval}


Ident = [a-zA-Z$_] [a-zA-Z0-9$_]*

IntLiteral = 0 | [1-9][0-9]*

BoolLiteral = true | false

new_line = \r|\n|\r\n;

white_space = {new_line} | [ \t\f]

%state STRING

%%

<YYINITIAL>{
/* keywords */
"int"             { return symbol("int",TYPE, new Integer( INTTYPE ) ); }
"if"              { return symbol("if",IF); }
"else"            { return symbol("else",ELSE); }
"while"           { return symbol("while",WHILE); }
"read"            { return symbol("read",READ); }
"write"           { return symbol("write",WRITE); }

/* names */
{Ident}           { return symbol("Identifier",IDENT, yytext()); }
  
/* string literals */

/* char literal */

/* bool literal */
{BoolLiteral} { return symbol("Boolconst",BOOLCONST, new Boolean(Boolean.parseBool(yytext()))); }

/* literals */
{IntLiteral} { return symbol("Intconst",INTCONST, new Integer(Integer.parseInt(yytext()))); }



/* separators */
  \"              { string.setLength(0); yybegin(STRING); }
";"               { return symbol("semicolon",SEMICOLON); }
","               { return symbol("comma",COMMA); }
"("               { return symbol("(",LPAR); }
")"               { return symbol(")",RPAR); }
"{"               { return symbol("{",BEGIN); }
"}"               { return symbol("}",END); }
"="               { return symbol("=",ASSIGN); }
"+"               { return symbol("plus",BINOP, new Integer( PLUS ) ); }
"-"               { return symbol("minus",BINOP, new Integer( MINUS ) ); }
"*"               { return symbol("mult",BINOP, new Integer( MULT ) ); }
"/"               { return symbol("div",BINOP, new Integer( DIV ) ); }
"%"               { return symbol("mod",BINOP, new Integer( MOD ) ); }
"<="              { return symbol("leq",COMP,  new Integer( LEQ ) ); }
">="              { return symbol("gtq",COMP,  new Integer( GTQ ) ); }
"=="              { return symbol("eq",COMP,  new Integer( EQ  ) ); }
"!="              { return symbol("neq",COMP,  new Integer( NEQ ) ); }
"<"               { return symbol("le",COMP,  new Integer( LE  ) ); }
">"               { return symbol("gt",COMP,  new Integer( GT  ) ); }
"&&"              { return symbol("and",BBINOP,new Integer( AND ) ); }
"||"              { return symbol("or",BBINOP,new Integer( OR  ) ); }
"!"               { return symbol("not",BUNOP); }



{white_space}     { /* ignore */ }

}

<STRING> {
  \"                             { yybegin(YYINITIAL); 
      return symbol("StringConst",STRINGCONST,string.toString(),string.length()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}


/* error fallback */
.|\n              {  /* throw new Error("Illegal character <"+ yytext()+">");*/
		    error("Illegal character <"+ yytext()+">");
                  }
