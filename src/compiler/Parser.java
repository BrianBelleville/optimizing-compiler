package compiler;

import java.io.File;
import ir.*;
import ir.instructions.*;
import support.*;

public class Parser {
    private BasicBlock currentBB;
    private BasicBlock currentJoinBlock;
    private IdentifierTable idTable;

    public Parser(File f, IdentifierTable t) throws Exception {
        scan = new Scanner(f, t);
	idTable = t;
    }

    // parse should return a cfg, should basically be a list of functions
    public void parse() throws Exception {
        computation();
    }

    private Scanner scan;

    // this should return the instruction that cooresponds to the last
    // value of the identifier, this will be from the scope table
    private Identifier ident() throws Exception
    {
        if(scan.sym == Token.ident) {
            scan.next();
	    return scan.idVal;
        } else {
            syntax_error("Identifier doesn't match regexp");
	    return null;
        }
    }

    // this should return an "instruction" that represents an
    // immediate value. This instruction won't be added to any basic
    // block, it will just be used for the argument to whatever
    // operation is being used.
    private void number() throws Exception
    {
        if(scan.sym == Token.num) {
            scan.next();
        } else {
            syntax_error("Number doesn't match regexp");
        }
    }

    // this should emit an adda instruction to get the offset of the
    // value in the array, multi-dimensional array will need to be
    // handled here, the array dimension will have to be known, so
    // will need to be recorded in the environment.
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

    // basically return the instruction that will represent the entire value.
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

    // maybe inside of expression do deleyed code generation as
    // presented in lecture to do partial evaluation of constant
    // expressions. Can be simplified from what was discussed in
    // lecture since we won't be worried about registers at this
    // point, just coalesce literal numbers if possible. Shouldn't be
    // too bad, for the productions that have an operation, if both of
    // the arguments are immediates, do the calculation and return a
    // new immediate. If not, emit code that does the operation using
    // the supplied args. If the expression turns out to be a literal,
    // don't even emit code at this point, for example if it is part
    // of a relation, it is possible you may not have to emit
    // anything.
    private Instruction expression() throws Exception
    {
        term();
        while(scan.sym == Token.add || scan.sym == Token.sub) {
            scan.next();
            term();
        }
	return null;
    }

    // this will emit the relation, which must be returned, the actual
    // branch will be emited by either the 'if' or 'while', making use
    // of what relation is returned. Possible to emit a literal
    // boolean in the case that the values of the operation can be
    // statically determined. In that case probably can eliminate some
    // dead code.
    private Cmp relation() throws Exception
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
	return null;
    }

    // shouldn't need to actually emit any code for the assignment,
    // the one exception being that if the result of the expression is
    // a literal, should emit the load immediate instruction. The main
    // thing that the assignment must do is update the join block with
    // Phis, and update the environment with the current value of the
    // variable.
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

    // this should result in a call instruction, if that is permited,
    // the exception being the built in methods which can just use the
    // single instructions in the IR
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

    // this should handle creating the different basic blocks and the
    // join node for the two branches.
    //
    // The if statement will need to create new basic blocks for each
    // branch, as well as a new basic block that will be the join
    // node/ next basic block following the if statement.
    private void ifStatement_rest() throws Exception
    {
	// won't know this until we see if there is an else branch
	BasicBlock branchTarget;

	// make basic blocks preemptivly
	BasicBlock oldCurrent = currentBB;
	BasicBlock oldJoin = currentJoinBlock;
	BasicBlock ifBB = new BasicBlock(oldCurrent);
	BasicBlock elseBB = new BasicBlock(oldCurrent);
	BasicBlock nextBB = new BasicBlock(oldCurrent);

	// if branch must exist
	oldCurrent.setFallThrough(ifBB);

	Cmp comp = relation();
        if(scan.sym != Token.then) {
            syntax_error("Misformed if statement, no 'then' keyword");
        }
        // sym == "then"
        scan.next();

	currentBB = ifBB;
	currentJoinBlock = nextBB;

        statSequence();
        if(scan.sym == Token.else_t) {
            scan.next();
	    currentBB = elseBB;
	    elseBB.setFallThrough(nextBB);

	    // the else block will be the target of the branch
	    oldCurrent.addInstruction(makeProperBranch(comp, elseBB));
	    // also add unconditional branch over the else block
	    ifBB.addInstruction(new Bra(nextBB));
	    statSequence();
        } else {
	    // otherwise, no else, so the next block will be the target of the branch
	    oldCurrent.addInstruction(makeProperBranch(comp, nextBB));
	    // ifBB will fall through
	    ifBB.setFallThrough(nextBB);
	}
	if(scan.sym == Token.fi) {
	    scan.next();
	    // todo: still need to push the phis from the join block out to the
	    // enclosing join block
	    currentBB = nextBB;
	    currentJoinBlock = oldJoin; // restore the join block
        } else {
            syntax_error("Misformed if statement, no 'fi' keyword");
        }
    }

    private BranchInstruction makeProperBranch(Cmp c, BasicBlock t) {
	switch (c.getType()) {
	case eq:
	    return new Bne(t);
	case neq:
	    return new Beq(t);
	case lt:
	    return new Bge(t);
	case lte:
	    return new Bgt(t);
	case gt:
	    return new Ble(t);
	case gte:
	    return new Blt(t);
	default:
	    return null;
	}
    }


    // the loop header needs to be the join node between the loop body
    // and the following code. One wrinkle, if a phi is created new,
    // need to go through the loop body and look for cases where the
    // old value was used and replace it with the value of the phi.
    // This seems like it will be one of the more difficult things to
    // do.
    //
    // The while statement will need to create a new basic block for
    // the loop header/join node as well as a new basic block for the
    // loop body, and a new basic block for the code that follows the
    // loop. The loop header will be dominated by what was previously
    // the current block, the loop header will dominate the loop body,
    // and the basic block that comes after the loop.
    private void whileStatement_rest() throws Exception
    {
	BasicBlock oldCurrent = currentBB;
	BasicBlock oldJoin = currentJoinBlock;
	BasicBlock loopHeader = new LoopHeader(oldCurrent);
	BasicBlock loopBody = new BasicBlock(loopHeader);
	BasicBlock nextBB = new BasicBlock(loopHeader);
	currentBB = loopHeader;
	currentJoinBlock = loopHeader; // relations can't perform assignment, so it's all good
	relation();
        if(scan.sym == Token.do_t) {
            scan.next();

	    currentBB = loopBody;
            statSequence();
            if(scan.sym == Token.od) {
                scan.next();
		currentBB = nextBB;
		currentJoinBlock = oldJoin; // still need to move phis out to the old join block
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
	    // after a return statement, you can eliminate any
	    // following code in the stat sequence.
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
	    scan.next();
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
	// look for error
	if(scan.sym == Token.semicolon)
	    scan.next();
	else
	    throw new Exception("var declaration: no semicolon terminating declaration");
    }

    // funcDecl should establish the basic block used as the start
    // point, then the other classes should extend as they go.
    // Probably the current basic block and the current join block
    // should be global state to the parser. Productions like while
    // and if will cause new basic blocks to be created.
    private Function funcDecl() throws Exception
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
		return null;
            } else {
                syntax_error("Function declaration: no ';' after function body");
            }
        } else {
            syntax_error("Function declaration: no ';' after formal parameters");
        }
	return null;		// never should be reached since syntax_error will always throw
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
        while(scan.sym == Token.var || scan.sym == Token.array) {
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
	    // variable declarations shouldn't actually cause any SSA
	    // to be emited, but should be stored in an environment.
	    // Globals can be handled with load/store instructions.
	    // Memory allocation for them will be at some other part
	    // of the compiler.
            while(scan.sym == Token.var || scan.sym == Token.array) {
                varDecl();
            }
            while(scan.sym == Token.function || scan.sym == Token.procedure) {
                funcDecl();
            }
	    // at this point, we can examine our environment for which
	    // globals have been used in other functions, and if used
	    // (either read or write) we can't optimize them using
	    // SSA, and instead can treat them as single cell arrays,
	    // I guess.
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
