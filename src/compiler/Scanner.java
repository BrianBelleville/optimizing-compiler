package compiler;

import java.io.File;
import support.*;

public class Scanner {
    // the current symbol being looked at
    public Token sym;
    
    // The current value of the symbol. If sym is a number, it will be
    // the integer value, val will not be set.
    public int val;
    // the current value of the symbol as an Identifier. If sym is
    // identifier, it will be the identifier read from the stream, if
    // sym is another token type, val will not be set.
    public Identifier idVal;
    private java.io.FileReader reader;
    private int next;

    private IdentifierTable idTable;

    // different patterns used by the scanner
    private String whitespace = " \t\n\013\f\r";
    private String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String numbers = "0123456789";
    private String alnum = letters + numbers;

    // this will set next to be the next character from the stream
    private void consume() throws Exception {
	next = reader.read();
    }

    private void comment() throws Exception {
	while(next != -1 && (char)next != '\n') {
	    consume();
	}
    }

    // the main interface to the scanner
    public void next() throws Exception {
	// eat whitespace
	while(whitespace.indexOf((char)next) != -1) {
	    consume();
	}
	if(next == -1) {
            sym = Token.eof;
            return;
        }
	// scan an identifier
	if(letters.indexOf((char)next) != -1) {
	    StringBuilder b = new StringBuilder();
	    b.append((char)next);
	    consume();
	    while(alnum.indexOf((char)next) != -1) {
		b.append((char)next);
		consume();
	    }
	    String s = b.toString();

	    // keywords are lexically identical to identifiers, so
	    // match against the list of keywords
	    switch (s) {
            case "let":
                sym = Token.let;
                return;
            case "call":
                sym = Token.call;
                return;
            case "if":
                sym = Token.if_t;
                return;
            case "then":
                sym = Token.then;
                return;
            case "else":
                sym = Token.else_t;
                return;
            case "fi":
                sym = Token.fi;
                return;
            case "while":
                sym = Token.while_t;
                return;
            case "do":
                sym = Token.do_t;
                return;
            case "od":
                sym = Token.od;
                return;
            case "return":
                sym = Token.return_t;
                return;
            case "var":
                sym = Token.var;
                return;
            case "array":
                sym = Token.array;
                return;
            case "function":
                sym = Token.function;
                return;
            case "procedure":
                sym = Token.procedure;
                return;
            case "main":
                sym = Token.main;
                return;
            }
	    sym = Token.ident;
	    idVal = idTable.addToTable(s);
	    return;
	}
	// scan numbers
	if(numbers.indexOf((char)next) != -1) {
	    StringBuilder b = new StringBuilder();
	    while(numbers.indexOf((char)next) != -1) {
		b.append((char)next);
		consume();
	    }
	    sym = Token.num;
	    val = new Integer(b.toString());
	    return;
	}
	if((char)next == '*') {
	    consume();
	    sym = Token.mul;
	    return;
	}
	if((char)next == '<') {
	    consume();
	    if((char)next == '=') {
		consume();
		sym = Token.lte;
		return;
	    }
	    if((char)next == '-') {
		consume();
		sym = Token.assign;
		return;
	    }
	    sym = Token.lt;
	    return;
	}
	if((char)next == '>') {
	    consume();
	    if((char)next == '=') {
		consume();
		sym = Token.gte;
		return;
	    }
	    sym = Token.gt;
	    return;
	}
	if((char)next == '=') {
	    consume();
	    if((char)next == '=') {
		consume();
		sym = Token.eq;
		return;
	    }
	    throw new Exception("Scan error: unrecognized operator '='");
	}
	if((char)next == '+') {
	    consume();
	    sym = Token.add;
	    return;
	}
	if((char)next == '-') {
	    consume();
	    sym = Token.sub;
	    return;
	}
	if((char)next == '(') {
	    consume();
	    sym = Token.openparen;
	    return;
	}
	if((char)next == ')') {
	    consume();
	    sym = Token.closeparen;
	    return;
	}
	if((char)next == '{') {
	    consume();
	    sym = Token.opencurly;
	    return;
	}
	if((char)next == '}') {
	    consume();
	    sym = Token.closecurly;
	    return;
	}
	if((char)next == '[') {
	    consume();
	    sym = Token.opensquare;
	    return;
	}
	if((char)next == ']') {
	    consume();
	    sym = Token.closesqare;
	    return;
	}
	if((char)next == ',') {
	    consume();
	    sym = Token.comma;
	    return;
	}
	if((char)next == '.') {
	    consume();
	    sym = Token.period;
	    return;
	}
	if((char)next == ';') {
	    consume();
	    sym = Token.semicolon;
	    return;
	}
	if((char)next == '#') {
	    comment();
	    next();
	    return;
	}
	if((char)next == '/') {
	    consume();
	    if((char)next == '/') {
		comment();
		next();
		return;
	    }
	    sym = Token.div;
	    return;
	}
	// if we are here, something went wrong
	throw new Exception("Scan error: unable to scan next token");
    }

    public Scanner(File f, IdentifierTable t) throws Exception{
	reader = new java.io.FileReader(f);
	idTable = t;
	next = reader.read();
	next();
    }
}
