package compiler;

import java.io.File;

public class Parser {
    public Parser(File f) throws Exception {
        scan = new Scanner(f);
    }

    public void parse() throws Exception {
        computation();
    }

    private Scanner scan;

    private void next() {};

    private void ident() throws Exception
    {
        if(scan.sym == Token.ident) {
            next();
        } else {
            syntax_error("Identifier doesn't match regexp");
        }
    }

    private void number() throws Exception
    {
        if(scan.sym == Token.num) {
            next();
        } else {
            syntax_error("Number doesn't match regexp");
        }
    }

    private void designator() throws Exception
    {
        ident();
        while(scan.sym == Token.opensquare) {
            scan.next();
            expression();
            if(scan.sym == Token.closesqare) {
                scan.next();
            } else {
                syntax_error("Designator: no closing ']'");
            }
        }
    }

    private void factor() throws Exception
    {
        if(scan.sym == Token.openparen) {
            scan.next();
            expression();
            if(scan.sym == Token.closeparen) {
                scan.next();
            } else {
                syntax_error("Factor: no closing ')'");
            }
        } else if (scan.sym == Token.call) {
            scan.next();
            funcCall_rest();
        } else if (scan.sym == Token.num) {
            scan.next();
        } else {
            designator();
        }
    }

    private void term() throws Exception
    {
        factor();
        while(scan.sym == Token.mul || scan.sym == Token.div) {
            scan.next();
            factor();
        }
    }

    private void expression() throws Exception
    {
        term();
        while(scan.sym == Token.add || scan.sym == Token.sub) {
            scan.next();
            term();
        }
    }

    private void relation() throws Exception
    {
        expression();
        if(scan.sym == Token.eq
           || scan.sym == Token.neq
           || scan.sym == Token.lt
           || scan.sym == Token.lte
           || scan.sym == Token.gt
           || scan.sym == Token.gte) {
            // relOp
            scan.next();
        } else {
            syntax_error("Relation: incorrect relation operator");
        }
        expression();
    }

    private void assignment_rest() throws Exception
    {
        designator();
        if(scan.sym == Token.assign) {
            scan.next();
        } else {
            syntax_error("Assignment: incorrect assignment operator");
        }
        expression();
    }

    private void funcCall_rest() throws Exception
    {
        ident();
        if(scan.sym == Token.openparen) {
            scan.next();
            if(scan.sym == Token.closeparen) {
                scan.next();
                return;
            }
            expression();
            while(scan.sym == Token.comma) {
                scan.next();
                expression();
            }
            if(scan.sym == Token.closeparen) {
                scan.next();
                return;
            }
            syntax_error("funcCall: no closing ')'");
        }
    }

    private void ifStatement_rest() throws Exception
    {
        relation();
        if(scan.sym != Token.then) {
            syntax_error("Misformed if statement, no 'then' keyword");
        }
        // sym == "then"
        scan.next();
        statSequence();
        if(scan.sym == Token.else_t) {
            scan.next();
            statSequence();
        }
        if(scan.sym == Token.fi) {
            scan.next();
        } else {
            syntax_error("Misformed if statement, no 'fi' keyword");
        }
    }

    private void whileStatement_rest() throws Exception
    {
        relation();
        if(scan.sym == Token.do_t) {
            scan.next();
            statSequence();
            if(scan.sym == Token.od) {
                scan.next();
            } else {
                syntax_error("While statement: no 'od'");
            }
        } else {
            syntax_error("While statement: no 'do'");
        }
    }

    private void returnStatement_rest() throws Exception
    {
        if(scan.sym == Token.closecurly || scan.sym == Token.semicolon)
            return;             // no expression
        expression();
    }

    private void statement() throws Exception
    {
        if(scan.sym == Token.let) {
            scan.next();
            assignment_rest();
        } else if(scan.sym == Token.call) {
            scan.next();
            funcCall_rest();
        } else if(scan.sym == Token.if_t) {
            scan.next();
            ifStatement_rest();
        } else if(scan.sym == Token.while_t) {
            scan.next();
            whileStatement_rest();
        } else if(scan.sym == Token.return_t) {
            scan.next();
            returnStatement_rest();
        } else {
            syntax_error("Statement: unrecognized statement type");
        }
    }

    private void statSequence() throws Exception
    {
        statement();
        while(scan.sym == Token.semicolon) {
            statement();
        }
    }

    private void typeDecl() throws Exception
    {
        if(scan.sym == Token.var) {
            scan.next();
        } else if (scan.sym == Token.array) {
            scan.next();
            if(scan.sym == Token.opensquare) {
                while(scan.sym == Token.opensquare) {
                    scan.next();
                    number();
                    if(scan.sym == Token.closesqare) {
                        scan.next();
                    } else {
                        syntax_error("Type declaration: no closing ']'");
                    }
                }
            } else {
                // must be at least one boundary description
                syntax_error("Type declaration: there must be at least one boundary description for an array");
            }
        }
    }

    private void varDecl() throws Exception
    {
        typeDecl();
        ident();
        while(scan.sym == Token.comma) {
            scan.next();
            ident();
        }
    }

    private void funcDecl() throws Exception
    {
        if(!(scan.sym == Token.function || scan.sym == Token.procedure)) {
            syntax_error("Function declaration: incorrect keyword");
        }
        // sym matches is function or procedure
        scan.next();
        ident();
        if(scan.sym != Token.semicolon) {
            formalParam();
        }
        // still need to look for this since formalParam may have
        // parsed successfully, but there could still be a syntax
        // error if no ';'
        if(scan.sym == Token.semicolon) {
            scan.next();
            funcBody();
            if(scan.sym == Token.semicolon) {
                scan.next();
            } else {
                syntax_error("Function declaration: no ';' after function body");
            }
        } else {
            syntax_error("Function declaration: no ';' after formal parameters");
        }
    }

    private void formalParam() throws Exception
    {
        if(scan.sym == Token.openparen) {
            scan.next();
            if(scan.sym != Token.closeparen) {
                ident();
                while(scan.sym == Token.comma) {
                    scan.next();
                    ident();
                }
            }
            if(scan.sym == Token.closeparen) {
                scan.next();
            } else {
                syntax_error("Formal parameters: no closing ')'");
            }
        } else {
            syntax_error("Formal parameters: no opening '('");
        }
    }

    private void funcBody() throws Exception
    {
        if(scan.sym == Token.var || scan.sym == Token.array) {
            varDecl();
        }
        if(scan.sym == Token.opencurly) {
            scan.next();
            if(scan.sym != Token.closecurly) {
                statSequence();
            }
            if(scan.sym == Token.closecurly) {
                scan.next();
            } else {
                syntax_error("Funcion body: no closing '}'");
            }
        } else {
            syntax_error("Function body: no opening '{'");
        }
    }

    private void computation() throws Exception
    {
        if(scan.sym == Token.main) {
            scan.next();
            while(scan.sym == Token.var || scan.sym == Token.array) {
                varDecl();
            }
            while(scan.sym == Token.function || scan.sym == Token.procedure) {
                funcDecl();
            }
            if(scan.sym == Token.opencurly) {
                scan.next();
                statSequence();
                if(scan.sym == Token.closecurly) {
                    scan.next();
                    if(scan.sym == Token.period) {
                        scan.next();
                    } else {
                        syntax_error("Computation: no '.' ending program");
                    }
                } else {
                    syntax_error("Computation: no '}' ending the statSequence");
                }
            } else {
                syntax_error("Computation: no '{' starting the statSequence");
            }
        } else {
            syntax_error("Computation: no 'main'");
        }
    }

    private void syntax_error(String msg) throws Exception
    {
        throw new Exception(msg);
    }
}
