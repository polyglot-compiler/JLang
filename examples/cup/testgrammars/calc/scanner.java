import java_cup.runtime.*;

class Main {
    public static void main(String[] argv) throws Exception{
	System.out.println("Please type your arithmethic expression:");
	Parser p = new Parser(new scanner());
	p.parse();
    }
}

public class scanner {
    /* single lookahead character */
    protected static int next_char;

    /* we use a SymbolFactory to generate Symbols */
    private SymbolFactory sf = new DefaultSymbolFactory();

    /* advance input by one character */
    protected void advance() throws java.io.IOException
    { next_char = System.in.read(); }

    /* initialize the scanner */
    public void init() throws java.io.IOException
    { advance(); }

    /* recognize and return the next complete token */
    public Symbol next_token() throws java.io.IOException
    {
	for (;;)
	    switch (next_char)
		{
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		    /* parse a decimal integer */
		    int i_val = 0;
		    do {
			i_val = i_val * 10 + (next_char - '0');
			advance();
		    } while (next_char >= '0' && next_char <= '9');
		    return sf.newSymbol("NUMBER",sym.NUMBER, new Integer(i_val));

		case ';': advance(); return sf.newSymbol("SEMI",sym.SEMI);
		case '+': advance(); return sf.newSymbol("PLUS",sym.PLUS);
		case '-': advance(); return sf.newSymbol("MINUS",sym.MINUS);
		case '*': advance(); return sf.newSymbol("TIMES",sym.TIMES);
		case '(': advance(); return sf.newSymbol("LPAREN",sym.LPAREN);
		case ')': advance(); return sf.newSymbol("RPAREN",sym.RPAREN);

		case -1: return sf.newSymbol("EOF",sym.EOF);

		default:
		    /* in this simple scanner we just ignore everything else */
		    advance();
		    break;
		}
    }
};
