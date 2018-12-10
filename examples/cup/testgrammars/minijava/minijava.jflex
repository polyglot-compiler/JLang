// Technische Universitaet Muenchen 
// Fakultaet fuer Informatik 

import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;

%%

%public
%class Lexer
%cup
%implements sym
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
"int"             { return symbol("int",TYPE, "INT" ); }
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
"+"               { return symbol("plus",BINOP,"ADD"  ); }
"-"               { return symbol("minus",BINOP, "SUB"  ); }
"*"               { return symbol("mul",BINOP, "MUL"  ); }
"/"               { return symbol("div",BINOP, "DIV"  ); }
"%"               { return symbol("mod",BINOP, "MOD"  ); }
"<="              { return symbol("leq",COMP,  "LEQ"  ); }
">="              { return symbol("geq",COMP,  "GEQ"  ); }
"=="              { return symbol("eq",COMP,  "EQ"   ); }
"!="              { return symbol("neq",COMP,  "NEQ"  ); }
"<"               { return symbol("less",COMP,  "LESS"   ); }
">"               { return symbol("gt",COMP,  "GT"   ); }
"&&"              { return symbol("and",BBINOP,"AND"  ); }
"||"              { return symbol("or",BBINOP,"OR"   ); }
"!"               { return symbol("not",BUNOP,"NOT"); }



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
