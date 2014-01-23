package compiler;

public class Parser {
    public void parse() throws Exception {
        computation();
    }

    // the next token to be parsed
    private String sym;

    // routine to consume the current token and advance to the next
    private void next() {};

    private void ident() throws Exception
    {
        if(sym.matches("^[a-z][a-z0-9]*$")) {
            next();
        } else {
            syntax_error("Identifier doesn't match regexp");
        }
    }

    private void number() throws Exception
    {
        if(sym.matches("^[0-9]+$")){
            next();
        } else {
            syntax_error("Number doesn't match regexp");
        }
    }

    private void designator() throws Exception
    {
        ident();
        while(sym.equals("[")) {
            next();
            expression();
            if(sym.equals("]")) {
                next();
            } else {
                syntax_error("Designator: no closing ']'");
            }
        }
    }

    private void factor() throws Exception
    {
        if(sym.equals("(")) {
            next();
            expression();
            if(sym.equals(")")) {
                next();
            } else {
                syntax_error("Factor: no closing ')'");
            }
        } else if (sym.equals("call")) {
            next();
            funcCall_rest();
        } else if (sym.matches("[0-9]+")) {
            // don't consume
            number();
        } else {
            designator();
        }
    }

    private void term() throws Exception
    {
        factor();
        while(sym.matches("\\*|/")) {
            next();
            factor();
        }
    }

    private void expression() throws Exception
    {
        term();
        while(sym.matches("\\+|-")) {
            next();
            term();
        }
    }

    private void relation() throws Exception
    {
        expression();
        if(sym.matches("==|!=|<|<=|>|>=")) {
            // relOp
            next();
        } else {
            syntax_error("Relation: incorrect relation operator");
        }
        expression();
    }

    private void assignment_rest() throws Exception
    {
        designator();
        if(sym.equals("<-")) {
            next();
        } else {
            syntax_error("Assignment: incorrect assignment operator");
        }
        expression();
    }

    private void funcCall_rest() throws Exception
    {
        ident();
	if(sym.equals("(")) {
	    next();
	    if(sym.equals(")")) {
		next();
		return;
	    }
	    expression();
	    while(sym.equals(",")) {
		next();
		expression();
	    }
	    if(sym.equals(")")) {
		next();
		return;
	    }
	    syntax_error("funcCall: no closing ')'");
	}
    }

    private void ifStatement_rest() throws Exception
    {
        relation();
	if(!sym.equals("then")) {
	    syntax_error("Misformed if statement, no 'then' keyword");
	}
	// sym == "then"
	next();
	statSequence();
	if(sym.equals("else")) {
	    next();
	    statSequence();
	}
	if(sym.equals("fi")) {
	    next();
	} else {
	    syntax_error("Misformed if statement, no 'fi' keyword");
	}
    }

    private void whileStatement_rest() throws Exception
    {
        relation();
        if(sym.equals("do")) {
            next();
            statSequence();
            if(sym.equals("od")) {
                next();
            } else {
                syntax_error("While statement: no 'od'");
            }
        } else {
            syntax_error("While statement: no 'do'");
        }
    }

    private void returnStatement_rest() throws Exception
    {
        if(sym.equals("}") || sym.equals(";"))
	    return; 		// no expression
        expression();
    }

    private void statement() throws Exception
    {
        if(sym.equals("let")) {
            next();
            assignment_rest();
        } else if(sym.equals("call")) {
            next();
            funcCall_rest();
        } else if(sym.equals("if")) {
            next();
            ifStatement_rest();
        } else if(sym.equals("while")) {
            next();
            whileStatement_rest();
        } else if(sym.equals("return")) {
            next();
            returnStatement_rest();
        } else {
            syntax_error("Statement: unrecognized statement type");
        }
    }

    private void statSequence() throws Exception
    {
        statement();
        while(sym.equals(";")) {
            statement();
        }
    }

    private void typeDecl() throws Exception
    {
        if(sym.equals("var")) {
            next();
        } else if (sym.equals("array")) {
            next();
            if(sym.equals("[")) {
                while(sym.equals("[")) {
                    next();
                    number();
                    if(sym.equals("]")) {
                        next();
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
        while(sym.equals(",")) {
            next();
            ident();
        }
    }

    private void funcDecl() throws Exception
    {
        if(!sym.matches("function|procedure")) {
	    syntax_error("Function declaration: incorrect keyword");
	}
	// sym matches "function|procedure"
	next();
	ident();
	if(!sym.equals(";")) {
	    formalParam();
	}
	// still need to look for this since formalParam may have
	// parsed successfully, but there could still be a syntax
	// error if no ';'
	if(sym.equals(";")) {
	    next();
	    funcBody();
	    if(sym.equals(";")) {
		next();
	    } else {
		syntax_error("Function declaration: no ';' after function body");
	    }
	} else {
	    syntax_error("Function declaration: no ';' after formal parameters");
	}
    }

    private void formalParam() throws Exception
    {
        if(sym.equals("(")) {
            next();
            if(!sym.equals(")")) {
                ident();
                while(sym.equals(",")) {
                    next();
                    ident();
                }
            }
            if(sym.equals(")")) {
                next();
            } else {
                syntax_error("Formal parameters: no closing ')'");
            }
        } else {
            syntax_error("Formal parameters: no opening '('");
        }
    }

    private void funcBody() throws Exception
    {
        if(sym.matches("var|array")) {
            varDecl();
        }
        if(sym.equals("{")) {
            next();
            if(!sym.equals("}")) {
                statSequence();
            }
            if(sym.equals("}")) {
                next();
            } else {
                syntax_error("Funcion body: no closing '}'");
            }
        } else {
            syntax_error("Function body: no opening '{'");
        }
    }

    private void computation() throws Exception
    {
        if(sym.equals("main")) {
            next();
            while(sym.matches("var|array")) {
                varDecl();
            }
            while(sym.matches("function|procedure")) {
                funcDecl();
            }
            if(sym.equals("{")) {
                next();
                statSequence();
                if(sym.equals("}")) {
                    next();
                    if(sym.equals(".")) {
                        next();
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
