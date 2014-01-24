package compiler;

import java.io.File;
import java.util.regex.Pattern;

public class Scanner {
    // the current symbol being looked at
    public Token sym;
    // The current value of the symbol. If sym is a number, it will be
    // the integer value, if sym is an identifier, it will be the
    // index in the symbol table, if sym is an other token, val will
    // not be set.
    public int val;

    private java.util.Scanner scan;

    // different patterns used by the scanner
    private Pattern identifier = Pattern.compile("[a-z][a-z0-9]*");
    private Pattern operators = Pattern.compile("==|!=|<|<=|>|>=|*|/|+|-|<-");
    private Pattern syntax = Pattern.compile("(|)|[|]|{|}|.|,|;");

    // the main interface to the scanner
    public void next() throws Exception {
        if(!scan.hasNext()) {
            sym = Token.eof;
            return;
        }
        if(scan.hasNext("//") || scan.hasNext("#")) {
            scan.nextLine();
            next();
            return;
        }
        if(scan.hasNext(identifier)) {
            String k = scan.next(identifier);
            // the keywords are lexically identical to an identifier
            switch (k) {
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
            // todo: insert the string into the string table
            val = 1;
            return;
        }
        if(scan.hasNext(operators)) {
            String r = scan.next(operators);
            switch (r) {
            case "==":
                sym = Token.eq;
                return;
            case "!=":
                sym = Token.neq;
                return;
            case "<":
                sym = Token.lt;
                return;
            case "<=":
                sym = Token.lte;
                return;
            case ">":
                sym = Token.gt;
                return;
            case ">=":
                sym = Token.gte;
                return;
            case "*":
                sym = Token.mul;
                return;
            case "/":
                sym = Token.div;
                return;
            case "+":
                sym = Token.add;
                return;
            case "-":
                sym = Token.sub;
                return;
            case "<-":
                sym = Token.assign;
                return;
            }
            // if we are here, something went wrong
            throw new Exception("Error scanning operator");
        }
        if(scan.hasNext(syntax)) {
            String s = scan.next(syntax);
            switch (s) {
            case "(":
                sym = Token.openparen;
                return;
            case ")":
                sym = Token.closeparen;
                return;
            case "[":
                sym = Token.opensquare;
                return;
            case "]":
                sym = Token.closesqare;
                return;
            case "{":
                sym = Token.opencurly;
                return;
            case "}":
                sym = Token.closecurly;
                return;
            case ".":
                sym = Token.period;
                return;
            case ",":
                sym = Token.comma;
                return;
            case ";":
                sym = Token.semicolon;
                return;
            }
            // if we are here, something went wrong
            throw new Exception("Error scanning syntax symbol");
        }
        if(scan.hasNextInt()) {
            val = scan.nextInt();
            sym = Token.num;
            return;
        }
        // if we got here, we don't know how to scan the input
        throw new Exception("Scan error: unable to tokenize next input");
    }
    public Scanner(File f) throws Exception{
        scan = new java.util.Scanner(f);
    }
}
