package compiler;

import java.io.File;
import java.util.ArrayList;
import ir.*;
import support.*;

public class Parser {
    private BasicBlock currentBB;
    private BasicBlock currentJoinBlock;
    private IdentifierTable idTable;
    private Environment env;
    private Scanner scan;
    private Function currentFunction;
    private MemoryRegion globals;
    private boolean inMain = false; // initially we are not in main

    // builtin function names
    private static Identifier inputNum = null;
    private static Identifier outputNum = null;
    private static Identifier outputNewLine = null;

    public Parser(File f, IdentifierTable t) throws Exception {
        idTable = t;
        scan = new Scanner(f, idTable);
        env = new Environment();

        // add builtin function names
        if(inputNum == null) {
            inputNum = idTable.addToTable("InputNum");
        }
        if(outputNum == null) {
            outputNum = idTable.addToTable("OutputNum");
        }
        if(outputNewLine == null) {
            outputNewLine = idTable.addToTable("OutputNewLine");
        }            
    }

    // parse should return a cfg, should basically be a list of functions
    public ArrayList<Function> parse() throws Exception {
        return computation();
    }

    // this should return the instruction that cooresponds to the last
    // value of the identifier, this will be from the scope table
    private Identifier ident() throws Exception
    {
        if(scan.sym == Token.ident) {
            // extract the value before advancing the input
            Identifier rval = scan.idVal;
            scan.next();
            return rval;
        } else {
            throw new Exception("Identifier doesn't match regexp");
        }
    }

    // this should return an "instruction" that represents an
    // immediate value. This instruction won't be added to any basic
    // block, it will just be used for the argument to whatever
    // operation is being used.
    private Immediate number() throws Exception
    {
        if(scan.sym == Token.num) {
            Immediate rval = new Immediate(scan.val);
            scan.next();
            return rval;
        } else {
            throw new Exception("Number doesn't match regexp");
        }
    }

    private Designator designator() throws Exception
    {
        Identifier id = ident();
        // if variable
        if(scan.sym != Token.opensquare) {
            return new Designator(id);
        }
        ArrayList<Value> index = new ArrayList<Value>();
        while(scan.sym == Token.opensquare) {
            scan.next();
            index.add(expression());
            if(scan.sym == Token.closesqare) {
                scan.next();
            } else {
                throw new Exception("Designator: no closing ']'");
            }
        }
        return new ArrayDesignator(id, index);
    }

    // basically return the instruction that will represent the entire value.
    private Value factor() throws Exception
    {
        Value rval;
        if(scan.sym == Token.openparen) {
            scan.next();
            rval = expression();
            if(scan.sym == Token.closeparen) {
                scan.next();
            } else {
                throw new Exception("Factor: no closing ')'");
            }
        } else if (scan.sym == Token.call) {
            scan.next();
            rval = funcCall_rest();
        } else if (scan.sym == Token.num) {
            rval = number();
        } else {
            Designator d = designator();
            Type t = env.getType(d.getVarName());
            rval = t.getValue(d, currentBB, env);
        }
        return rval;
    }

    // term and expression will perform the computations at compile
    // time if the operands are both immediate values. This can be
    // taken further by reordering sets of long computations so that
    // more computation can be done at compile time. For example since
    // the grammar is parsed left to right, the expression (i * 2 * 2)
    // will result in 2 muls, but we can reorder it to (2 * 2 * i) and
    // then simplify to (4 * i) to only emit a single mul.
    private Value term() throws Exception
    {
        Value previous, rval;
        previous = rval = factor();
        while(scan.sym == Token.mul || scan.sym == Token.div) {
            Token op = scan.sym;
            scan.next();
            previous = rval;
            Value next = factor();
	    Instruction ins;
	    if(op == Token.mul) {
		ins = new Mul(previous, next);
	    } else {            // op == Token.div
		ins = new Div(previous, next);
	    }
	    currentBB.addInstruction(ins);
	    rval = ins;
	}
        return rval;
    }

    private Value expression() throws Exception
    {
        Value previous, rval;
        previous = rval = term();
        while(scan.sym == Token.add || scan.sym == Token.sub) {
            Token op = scan.sym;
            scan.next();
            previous = rval;
            Value next = term();
	    Instruction ins;
	    if(op == Token.add) {
		ins = new Add(previous, next);
	    } else {            // op == Token.sub
		ins = new Sub(previous, next);
	    }
	    currentBB.addInstruction(ins);
	    rval = ins;
	}
        return rval;
    }

    // this will emit the relation, which must be returned, the actual
    // branch will be emited by either the 'if' or 'while', making use
    // of what relation is returned. Possible to emit a literal
    // boolean in the case that the values of the operation can be
    // statically determined. In that case probably can eliminate some
    // dead code.
    private Cmp relation() throws Exception
    {
        Value left = expression();
        Cmp.CmpType op;
        // relOp
        switch (scan.sym) {
        case eq:
            op = Cmp.CmpType.eq;
            break;
        case neq:
            op = Cmp.CmpType.neq;
            break;
        case lt:
            op = Cmp.CmpType.lt;
            break;
        case lte:
            op = Cmp.CmpType.lte;
            break;
        case gt:
            op = Cmp.CmpType.gt;
            break;
        case gte:
            op = Cmp.CmpType.gte;
            break;
        default:
            throw new Exception("Relation: incorrect relation operator");
        }
        scan.next();
        Value right = expression();
        Cmp rval =  new Cmp(op, left, right);
        currentBB.addInstruction(rval);
        return rval;
    }

    // shouldn't need to actually emit any code for the assignment,
    // the one exception being that if the result of the expression is
    // a literal, should emit the load immediate instruction. The main
    // thing that the assignment must do is update the join block with
    // Phis, and update the environment with the current value of the
    // variable.
    private void assignment_rest() throws Exception
    {
        Designator d = designator();
        if(scan.sym == Token.assign) {
            scan.next();
        } else {
            throw new Exception("Assignment: incorrect assignment operator");
        }
        Value v = expression();
	Type t = env.getType(d.getVarName());
        t.assignValue(d, currentBB, currentJoinBlock, env, v);
    }

    // this should result in a call instruction, if that is permited,
    // the exception being the built in methods which can just use the
    // single instructions in the IR
    private Value funcCall_rest() throws Exception
    {
        Identifier funcName = ident();
        ArrayList<Value> args = new ArrayList<Value>();
        do {
            if(scan.sym == Token.openparen) {
                scan.next();
                if(scan.sym == Token.closeparen) {
                    scan.next();
                    break;
                }
                args.add(expression());
                while(scan.sym == Token.comma) {
                    scan.next();
                    args.add(expression());
                }
                if(scan.sym == Token.closeparen) {
                    scan.next();
                    break;
                }
                throw new Exception("funcCall: no closing ')'");
            }
        } while (false);
        
        Instruction rval = makeCall(funcName, args);
        currentBB.addInstruction(rval);
        return rval;
    }

    
    private Instruction makeCall(Identifier name, ArrayList<Value> args)
        throws Exception {
        if(name.equals(inputNum)) {
            if(args.size() != 0) {
                throw new Exception("Incorrect number of arguments for builtin function");
            }
            return new Read();
        }
        if(name.equals(outputNum)) {
            if(args.size() != 1) {
                throw new Exception("Incorrect number of arguments for builtin function");
            }
            return new Write(args.get(0));
        }
        if(name.equals(outputNewLine)) {
            if(args.size() != 0) {
                throw new Exception("Incorrect number of arguments for builtin function");
            }
            return new Wln();
        }
        return new Call(name, args);
    }

    // this should handle creating the different basic blocks and the
    // join node for the two branches.
    //
    // The if statement will need to create new basic blocks for each
    // branch, as well as a new basic block that will be the join
    // node/ next basic block following the if statement.
    private void ifStatement_rest() throws Exception
    {
        // make basic blocks preemptivly
        BasicBlock oldCurrent = currentBB;
        BasicBlock oldJoin = currentJoinBlock;
        BasicBlock ifStartBB = new BasicBlock(oldCurrent);
        BasicBlock elseStartBB = new BasicBlock(oldCurrent);
        BasicBlock nextBB = new BasicBlock(oldCurrent);

        // if branch must exist
        oldCurrent.setFallThrough(ifStartBB);

        Cmp comp = relation();
        if(scan.sym != Token.then) {
            throw new Exception("Misformed if statement, no 'then' keyword");
        }
        // sym == "then"
        scan.next();

	// set currentBB to be the start of the 'if' body
        currentBB = ifStartBB;
	// set the join block to be the BB that will follow the 'if'
	// statement
        currentJoinBlock = nextBB;

	// generate code for the 'if' body
        currentJoinBlock.setCurrentBranch(1);
        env.enter();
        statSequence();
        env.exit();
        currentJoinBlock.setIncomingBranch(currentBB);

        currentJoinBlock.setCurrentBranch(2);
        if(scan.sym == Token.else_t) {
	    scan.next();

            // the else block will be the target of the branch
            oldCurrent.addInstruction(makeProperBranch(comp, elseStartBB));
            // also add unconditional branch from the end of the 'if'
            // statSequence over the else block
            currentBB.addInstruction(new Bra(nextBB));

	    // set the new current, this must be done AFTER the
	    // branches are emited
            currentBB = elseStartBB;

	    // generate code for the 'else' body
            env.enter();
            statSequence();
            env.exit();
            currentBB.setFallThrough(nextBB);
            currentJoinBlock.setIncomingBranch(currentBB);
        } else {
            // otherwise, no else, so the next block will be the target of the branch
            oldCurrent.addInstruction(makeProperBranch(comp, nextBB));
            // 'if' branch will fall through
            currentBB.setFallThrough(nextBB);
            currentJoinBlock.setIncomingBranch(oldCurrent);
        }
        if(scan.sym == Token.fi) {
            scan.next();
	    currentBB = nextBB;
            currentJoinBlock = oldJoin; // restore the join block
	    // propogate the phis from this join block to the enclosing join block
	    currentBB.propagatePhis(env, currentJoinBlock);
        } else {
            throw new Exception("Misformed if statement, no 'fi' keyword");
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
        BasicBlock loopBodyStart = new BasicBlock(loopHeader);
        BasicBlock nextBB = new BasicBlock(loopHeader);
        // the previous basic block will fall through to the header
        oldCurrent.setFallThrough(loopHeader);
        // and the header will fall through to the loop body
        loopHeader.setFallThrough(loopBodyStart);

        // set up the incomming basic block for the loop header
        loopHeader.setCurrentBranch(1);
        loopHeader.setIncomingBranch(oldCurrent);
        
        currentBB = loopHeader;
        currentJoinBlock = loopHeader; // relations can't perform assignment, so it's all good
        Cmp comp = relation();
        // add the branch instruction
        loopHeader.addInstruction(makeProperBranch(comp, nextBB));
        loopHeader.setCurrentBranch(2);
        if(scan.sym == Token.do_t) {
            scan.next();
            currentBB = loopBodyStart;
            env.enter();
            statSequence();
            // add branch to loop header
            currentBB.addInstruction(new Bra(loopHeader));
            env.exit();

            loopHeader.setIncomingBranch(currentBB);

            if(scan.sym == Token.od) {
                scan.next();
                currentBB = nextBB;
                currentJoinBlock = oldJoin;
		// move phis in the loop header out to the old join
		// block
		loopHeader.propagatePhis(env, currentJoinBlock);
            } else {
                throw new Exception("While statement: no 'od'");
            }
        } else {
            throw new Exception("While statement: no 'do'");
        }
    }

    private void returnStatement_rest() throws Exception
    {
        Instruction r = null;        
        if(scan.sym == Token.closecurly || scan.sym == Token.semicolon) {
            // if there is no expression to be returned
            r = inMain ? new End() : new Ret();
        } else {
            // else there is an expression to be returned
            if(inMain) {
                throw new Exception("Attempt to return value from __MAIN__");
            }
            r = new Ret(expression());
        }
        currentBB.addInstruction(r);
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
            throw new Exception("Statement: unrecognized statement type");
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

    private Type typeDecl(boolean global) throws Exception
    {
        if(scan.sym == Token.var) {
            scan.next();
            return new VarType(global, globals);
        } else if (scan.sym == Token.array) {
            scan.next();
            if(scan.sym == Token.opensquare) {
                ArrayList<Integer> dimension = new ArrayList<Integer>();
                while(scan.sym == Token.opensquare) {
                    scan.next();
                    int i = number().getValue();
                    dimension.add(i);
                    if(scan.sym == Token.closesqare) {
                        scan.next();
                    } else {
                        throw new Exception("Type declaration: no closing ']'");
                    }
                }
                return new ArrayType(dimension, global, globals, currentFunction.locals);
            } else {
                // must be at least one boundary description
                throw new Exception("Type declaration: there must be at least one boundary description for an array");
            }
        } else {
            throw new Exception("Unexpected type declaration");
        }
    }

    private Value defaultVariableValue(Identifier varName) {
        return Globals.zeroInitializeVariables ? new Immediate(0) : new DefaultValue(varName);
    }

    private void varDecl(boolean global) throws Exception
    {
        Type type = typeDecl(global);
        Identifier varName = ident();
        // todo: use the default value to detect uninitialized variables
        env.put(varName, defaultVariableValue(varName));
        env.putType(varName, type);
        while(scan.sym == Token.comma) {
            scan.next();
            varName = ident();
            env.put(varName, defaultVariableValue(varName));
            env.putType(varName, type);
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
        Function rval = new Function();
        currentFunction = rval;
        currentBB = new BasicBlock(null);
        currentJoinBlock = null;
        rval.entryPoint = currentBB;
        env.enter();
        if(!(scan.sym == Token.function || scan.sym == Token.procedure)) {
            throw new Exception("Function declaration: incorrect keyword");
        }
        // sym matches function or procedure
        scan.next();
        rval.name = ident();
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
                env.exit();
                return rval;
            } else {
                throw new Exception("Function declaration: no ';' after function body");
            }
        } else {
            throw new Exception("Function declaration: no ';' after formal parameters");
        }
    }

    private void formalParam() throws Exception
    {
        if(scan.sym == Token.openparen) {
            scan.next();
            if(scan.sym != Token.closeparen) {
                int argPosition = 0;
                Identifier i = ident();
                Instruction getArg = new Fetch(i, argPosition);
                currentBB.addInstruction(getArg);
                env.put(i, getArg);
                env.putType(i, new VarType(false, globals));
                argPosition++;
                while(scan.sym == Token.comma) {
                    scan.next();
                    i = ident();
                    getArg = new Fetch(i, argPosition);
                    currentBB.addInstruction(getArg);
                    env.put(i, getArg);
                    env.putType(i, new VarType(false, globals));
                    argPosition++;
                }
            }
            if(scan.sym == Token.closeparen) {
                scan.next();
            } else {
                throw new Exception("Formal parameters: no closing ')'");
            }
        } else {
            throw new Exception("Formal parameters: no opening '('");
        }
    }

    private void funcBody() throws Exception
    {
        while(scan.sym == Token.var || scan.sym == Token.array) {
            varDecl(false);
        }
        if(scan.sym == Token.opencurly) {
            scan.next();
            if(scan.sym != Token.closecurly) {
                statSequence();
            }
            if(scan.sym == Token.closecurly) {
                scan.next();
                if(!currentBB.hasFinalReturn()) {
                    // make sure all functions end in a return
                    currentBB.addInstruction(new Ret()); 
                }
            } else {
                throw new Exception("Function body: no closing '}'");
            }
        } else {
            throw new Exception("Function body: no opening '{'");
        }
    }

    private ArrayList<Function> computation() throws Exception
    {
        Function main = new Function();
        globals = new GlobalMemoryRegion();
        ArrayList<Function> rval = new ArrayList<Function>();
        env.enter();
        if(scan.sym == Token.main) {
            scan.next();
            // variable declarations shouldn't actually cause any SSA
            // to be emited, but will be stored in env
            while(scan.sym == Token.var || scan.sym == Token.array) {
                // these are global variables, but could be optimized to become main local variables.
                currentFunction = main; 
                varDecl(true);
            }
            while(scan.sym == Token.function || scan.sym == Token.procedure) {
                rval.add(funcDecl());
            }
            // at this point, we can examine our environment for which
            // globals have been used in other functions,
            env.freezeGlobals();
            if(scan.sym == Token.opencurly) {
                inMain = true;  // we are now parsing the main procedure
                currentBB = new BasicBlock(null);
                currentJoinBlock = null;
                currentFunction = main;
                main.name = idTable.addToTable("__MAIN__");
                main.entryPoint = currentBB;
                rval.add(main);
                scan.next();
                statSequence();
                if(!currentBB.hasFinalEnd()) {
                    currentBB.addInstruction(new End());
                }
                if(scan.sym == Token.closecurly) {
                    scan.next();
                    if(scan.sym == Token.period) {
                        scan.next();
                        env.exit();
                        return rval;
                    } else {
                        throw new Exception("Computation: no '.' ending program");
                    }
                } else {
                    throw new Exception("Computation: no '}' ending the statSequence");
                }
            } else {
                throw new Exception("Computation: no '{' starting the statSequence");
            }
        } else {
            throw new Exception("Computation: no 'main'");
        }
    }
}
