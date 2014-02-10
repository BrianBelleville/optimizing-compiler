package compiler;

import java.io.File;
import java.util.ArrayList;

import ir.*;
import ir.base.*;
import support.*;

public class Parser {
    private BasicBlock currentBB;
    private BasicBlock currentJoinBlock;
    private IdentifierTable idTable;
    private Environment env;
    private Scanner scan;

    public Parser(File f, IdentifierTable t) throws Exception {
        idTable = t;
        scan = new Scanner(f, idTable);
        env = new Environment();
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

    // this should emit an adda instruction to get the offset of the
    // value in the array, multi-dimensional array will need to be
    // handled here, the array dimension will have to be known, so
    // will need to be recorded in the environment.
    //
    // todo: implement designators. This will require having a scope
    // environment data structure.
    private Designator designator() throws Exception
    {
        Identifier id = ident();
        // todo: handle arrays, should emit the indexing, adda, and load all in here
        while(scan.sym == Token.opensquare) {
            throw new Exception("Array designators not implemented");

            // scan.next();
            // expression();
            // if(scan.sym == Token.closesqare) {
            //     scan.next();
            // } else {
            //     throw new Exception("Designator: no closing ']'");
            // }
        }
        return new Designator(id);
    }

    class Designator {
        private Identifier var;
        public Designator(Identifier v) {
            var = v;
        }
        public void emitAssign(BasicBlock cur, BasicBlock join, Environment env, Value newVal)
            throws Exception {
            Value old = env.get(var);
            env.put(var, newVal);
            if(join != null) {
                join.addPhi(var, old, newVal);
            }
        }
        // may need to emit load for arrays/ globals
        public Value getValue(BasicBlock cur) {
            return env.get(var);
        }
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
            rval = designator().getValue(currentBB);
        }
        return rval;
    }

    // todo: inside of expression and term do delayed code generation
    // as presented in lecture to do partial evaluation of constant
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
	d.emitAssign(currentBB, currentJoinBlock, env, v);
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
        // todo: check for builtin functions
        Instruction rval = new Call(funcName, args);
        currentBB.addInstruction(rval);
        return rval;
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
            throw new Exception("Misformed if statement, no 'then' keyword");
        }
        // sym == "then"
        scan.next();

        currentBB = ifBB;
        currentJoinBlock = nextBB;
        // phi's will be generated in the join block, exit the
        // environment so that assignments along this path will not
        // conflict with those in the next BB
        env.enter();
        statSequence();
        env.exit();
        if(scan.sym == Token.else_t) {
            scan.next();
            currentBB = elseBB;
            elseBB.setFallThrough(nextBB);

            // the else block will be the target of the branch
            oldCurrent.addInstruction(makeProperBranch(comp, elseBB));
            // also add unconditional branch over the else block
            ifBB.addInstruction(new Bra(nextBB));

            env.enter();
            statSequence();
            env.exit();
        } else {
            // otherwise, no else, so the next block will be the target of the branch
            oldCurrent.addInstruction(makeProperBranch(comp, nextBB));
            // ifBB will fall through
            ifBB.setFallThrough(nextBB);
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
        BasicBlock loopBody = new BasicBlock(loopHeader);
        BasicBlock nextBB = new BasicBlock(loopHeader);
        // the previous basic block will fall through to the header
        oldCurrent.setFallThrough(loopHeader);
        // and the header will fall through to the loop body
        loopHeader.setFallThrough(loopBody);
        currentBB = loopHeader;
        currentJoinBlock = loopHeader; // relations can't perform assignment, so it's all good
        Cmp comp = relation();
        // add the branch instruction
        loopHeader.addInstruction(makeProperBranch(comp, nextBB));
        if(scan.sym == Token.do_t) {
            scan.next();
            currentBB = loopBody;
            env.enter();
            statSequence();
            // add branch to loop header
            currentBB.addInstruction(new Bra(loopHeader));
            env.exit();

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

    // todo: somehow we need to provide type information to the
    // environment
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
                        throw new Exception("Type declaration: no closing ']'");
                    }
                }
            } else {
                // must be at least one boundary description
                throw new Exception("Type declaration: there must be at least one boundary description for an array");
            }
        }
    }

    private void varDecl() throws Exception
    {
        typeDecl();
        Identifier varName = ident();
        // todo: use the default value to detect uninitialized variables
        env.put(varName, new NamedValue("var_" + varName.getString()));
        while(scan.sym == Token.comma) {
            scan.next();
            varName = ident();
            env.put(varName, new NamedValue("var_" + varName.getString()));
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
                Identifier i = ident();
                env.put(i, new NamedValue("arg_" + i.getString()));
                while(scan.sym == Token.comma) {
                    scan.next();
                    i = ident();
                    env.put(i, new NamedValue("arg_" + i.getString()));
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
                throw new Exception("Funcion body: no closing '}'");
            }
        } else {
            throw new Exception("Function body: no opening '{'");
        }
    }

    private ArrayList<Function> computation() throws Exception
    {
        ArrayList<Function> rval = new ArrayList<Function>();
        env.enter();
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
                rval.add(funcDecl());
            }
            // at this point, we can examine our environment for which
            // globals have been used in other functions, and if used
            // (either read or write) we can't optimize them using
            // SSA, and instead can treat them as single cell arrays,
            // I guess.
            if(scan.sym == Token.opencurly) {
                currentBB = new BasicBlock(null);
                currentJoinBlock = null;
                Function main = new Function();
                main.name = idTable.addToTable("__MAIN__");
                main.entryPoint = currentBB;
                rval.add(main);
                scan.next();
                statSequence();
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
