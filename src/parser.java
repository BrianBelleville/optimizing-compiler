public class parser {
    public void parse() {
	computation();
    }

    // the next token to be parsed
    private String sym;

    // routine to consume the current token and advance to the next
    private void next() {};

    private void ident()
    {
	if(sym.matches("^[a-z][a-z0-9]*$")) {
	    next();
	} else {
	    syntax_error();
	}
    }

    private void number()
    {
	if(sym.matches("^[0-9]+$")){
	    next();
	} else {
	    syntax_error();
	}
    }

    private void designator()
    {
	ident();
	while(sym.equals("[")) {
	    next();
	    expression();
	    if(sym.equals("]")) {
		next();
	    } else {
		syntax_error();
	    }
	}
    }

    private void factor()
    {
	if(sym.equals("(")) {
	    next();
	    expression();
	    if(sym.equals(")")) {
		next();
	    } else {
		syntax_error();
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

    private void term()
    {
	factor();
	while(sym.matches("\\*|/")) {
	    next();
	    factor();
	}
    }

    private void expression()
    {
	term();
	while(sym.matches("\\+|-")) {
	    next();
	    term();
	}
    }

    private void relation()
    {
	expression();
	if(sym.matches("==|!=|<|<=|>|>=")) {
	    // relOp
	    next();
	} else {
	    syntax_error();
	}
	expression();
    }

    private void assignment_rest()
    {
	designator();
	if(sym.equals("<-")) {
	    next();
	} else {
	    syntax_error();
	}
	expression();
    }

    private void funcCall_rest()
    {
	ident();
	if(sym.equals("(")) {
	    next();
	    if(sym.equals(")")) {
		next();
	    } else {
		expression();
		while(sym.equals(",")) {
		    next();
		    expression();
		}
	    }
	}
    }

    private void ifStatement_rest()
    {
	relation();
	if(sym.equals("then")) {
	    next();
	    statSequence();
	    if(sym.equals("else")) {
		next();
		statSequence();
	    }
	    if(sym.equals("fi")) {
		next();
	    } else {
		syntax_error();
	    }
	} else {
	    syntax_error();
	}
    }

    private void whileStatement_rest()
    {
	relation();
	if(sym.equals("do")) {
	    next();
	    statSequence();
	    if(sym.equals("od")) {
		next();
	    } else {
		syntax_error();
	    }
	} else {
	    syntax_error();
	}
    }

    private void returnStatement_rest()
    {
	// todo: handle the case where there is no expression
	expression();
    }

    private void statement()
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
	    syntax_error();
	}
    }

    private void statSequence()
    {
	statement();
	while(sym.equals(";")) {
	    statement();
	}
    }

    private void typeDecl()
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
			syntax_error();
		    }
		}
	    } else {
		// must be at least one boundary description
		syntax_error();
	    }
	}
    }

    private void varDecl()
    {
	typeDecl();
	ident();
	while(sym.equals(",")) {
	    next();
	    ident();
	}
    }

    private void funcDecl()
    {
	if(sym.matches("function|procedure")) {
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
		    syntax_error();
		}
	    } else {
		syntax_error();
	    }
	} else {
	    syntax_error();
	}
    }

    private void formalParam()
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
		syntax_error();
	    }
	} else {
	    syntax_error();
	}
    }

    private void funcBody()
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
		syntax_error();
	    }
	} else {
	    syntax_error();
	}
    }
    
    private void computation()
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
			syntax_error();
		    }
		} else {
		    syntax_error();
		}
	    }
	} else {
	    syntax_error();
	}
    }		    

    private void syntax_error()
    {
	
    }
}
