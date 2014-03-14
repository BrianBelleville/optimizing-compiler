package ir;

import transform.Pass;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.ArrayList;
import support.Environment;
import support.Identifier;
import support.InterferenceGraph;
import ir.Ret;
import ir.End;
import java.io.Writer;

// todo: have a table containing the most recent instance of a
//       opcode, this can be used to perform CSE faster
public class BasicBlock {
    private BasicBlock dominator;
    public LinkedList<Instruction> instructions;
    private BasicBlock fallThrough;
    private static int blockNum = 0;
    private int number;
    protected int localPass = -1;
    protected static int currentPass = 0;
    protected int localLiveRangePass = -1;
    protected static int currentLiveRangePass = 0;
    private boolean printed = false;
    private Instruction mostRecentDominating;
    private HashSet<Value> live;
    private int currentBranch = 1; // always initially generate incomming branch 1
    private BasicBlock incomingBranch1;
    private BasicBlock incomingBranch2;
    private BasicBlock next;    // link to the next block as the code is layed out in program memory

    public void setNext(BasicBlock bb) {
        next = bb;
    }

    public BasicBlock getNext() {
        return next;
    }

    public boolean isVisited() {
        return localPass == currentPass;
    }

    public void visit() {
        localPass = currentPass;
    }

    public static void incrementGlobalPass() {
        currentPass++;
    }

    private int getNextBlockNum() {
        blockNum += 1;
        return blockNum;
    }

    public HashSet<Value> getLive() {
        return live;
    }

    public void setCurrentBranch(int i) {
        assert (i >= 1 && i <= 2);
        currentBranch = i;
    }

    public void setIncomingBranch(BasicBlock b) {
        assert (currentBranch >= 1 && currentBranch <= 2);
        if(currentBranch == 1) {
            assert (incomingBranch1 == null); // only set once
            incomingBranch1 = b;
        } else if (currentBranch == 2) {
            assert (incomingBranch2 == null);
            incomingBranch2 = b;
        }
    }

    public boolean hasFinalReturn() {
        // if empty return false, otherwise check if the last instruction is a Ret
        return !instructions.isEmpty() && instructions.getLast() instanceof Ret;
    }

    public boolean hasFinalEnd() {
        // if empty return false, otherwise check if the last instruction is an End
        return !instructions.isEmpty() && instructions.getLast() instanceof End;
    }

    public BasicBlock(BasicBlock dominator) {
        this.dominator = dominator;
        number = getNextBlockNum();
        instructions = new LinkedList<Instruction>();
        live = new HashSet<Value>();
        if(this.dominator != null) {
            mostRecentDominating = this.dominator.mostRecentDominating;
        } else {
            mostRecentDominating = null;
        }
    }

    public String getNodeName() {
        return "b_" + Integer.toString(number);
    }

    public boolean isEmpty() {
	return instructions.isEmpty();
    }

    public void printBlockLayoutOrder(Writer w) throws Exception {
        w.write(getNodeName() + " [label=\"" + getNodeName() + "\\l");
        for(Instruction i : instructions) {
            i.printInstruction(w);
            w.write("\\l");
        }
        w.write("\"]\n");
        if(next != null) {
            w.write(getNodeName() + " -> " + next.getNodeName() + ";\n");
            next.printBlock(w);
        }
    }

    public void printBlock(Writer w) throws Exception {
        if(!printed) {
            printed = true;
            w.write(getNodeName() + " [label=\"" + getNodeName() + "\\l");
            for(Instruction i : instructions) {
                i.printInstruction(w);
                w.write("\\l");
            }
            w.write("\"]\n");
            if(fallThrough != null) {
                w.write(getNodeName() + " -> " + fallThrough.getNodeName() + ";\n");
                fallThrough.printBlock(w);
            }
            BasicBlock branch = getBranchTarget();
            if(branch != null) {
                w.write(getNodeName() + " -> " + branch.getNodeName() + ";\n");
                branch.printBlock(w);
            }
            if(dominator != null) {
                w.write(getNodeName() + " -> " + dominator.getNodeName() + "[color=\"green\"];\n");
            }
        }
    }

    public BasicBlock getFallThrough() {
        return fallThrough;
    }
    public void setFallThrough(BasicBlock b) {
        fallThrough = b;
        // if the code will fall through to b, that will be the next block emited as machine code
        next = b;
    }

    public BasicBlock getBranchTarget() {
        if(instructions.isEmpty()) {
            return null;
        }
        Instruction i = instructions.get(instructions.size() - 1);
        if(i instanceof BranchInstruction) {
            return ((BranchInstruction)i).getTarget();
        }
        return null;
    }
    public void addInstruction(Instruction i) throws Exception {
        i.setDominating(mostRecentDominating);
        i.setContainingBB(this);
        instructions.add(i);
        mostRecentDominating = i;
    }

    public void makeMovesForPhis() {
        makeMovesForPhisInternal();
        currentPass++;
    }

    private void makeMovesForPhisInternal() {
        if(localPass == currentPass) {
            return;
        }
        localPass = currentPass;
        ArrayList<Phi> phis = new ArrayList<Phi>();
        for(Instruction i : instructions) {
            if(i instanceof Phi) {
                phis.add((Phi)i);
            } else {
                break;
            }
        }

        // todo: we need to do a topological sorting of phi
        // instructions here, and insert temporaries to break cycles.
        for(Phi p : phis) {
            Integer color = p.getColor();
            {
                // insert move for first arg
                Move m1 = new Move(p.getArg1());
                m1.setColor(color);
                incomingBranch1.addAtEndBeforeBranch(m1);
            }
            {
                // insert move for second arg
                Move m2 = new Move(p.getArg2());
                m2.setColor(color);
                incomingBranch2.addAtEndBeforeBranch(m2);
            }
        }

        BasicBlock ch1 = getFallThrough();
        BasicBlock ch2 = getBranchTarget();
        if(ch1 != null) {
            ch1.makeMovesForPhisInternal();
        }
        if(ch2 != null) {
            ch2.makeMovesForPhisInternal();
        }

    }

    private void addAtEndBeforeBranch(Instruction i) {
        if(!instructions.isEmpty() &&
           instructions.getLast() instanceof BranchInstruction) {
            ListIterator<Instruction> iter =
                instructions.listIterator(instructions.size() - 1);
            iter.next();        // returns last instruction
            // also returns the last instructions, but we are now 'going backwards'
            Instruction cursor = iter.previous();
            // need to put the instruction before the compare and
            // branch, a compare instruction may not be present though
            assert(cursor instanceof BranchInstruction); // we just tested this

            // if the basic block has no other instructions, then we
            // can insert the instruction immediatly, otherwise we
            // need to see if there is also a Cmp
            if(iter.hasPrevious()) {
                cursor = iter.previous();

                // if we did not get a cmp, we went too far
                if(!(cursor instanceof Cmp)) {
                    // will return the same instruction as cursor, but now instructions will be added after it
                    iter.next();
                }
                // but if we did get a Cmp, we went just the right amount
            }
            iter.add(i);
        } else {
            instructions.add(i);
        }
    }

    public void addPhi(Identifier var, Value oldVal, Value newVal)
        throws Exception
    {
        addPhiInternal(var, oldVal, newVal);
    }

    public void propagatePhis(Environment env, BasicBlock joinBlock) throws Exception {
	for(Instruction i : instructions) {
	    if(i instanceof Phi) {
		Phi phi = (Phi)i;
		Identifier variable = phi.getVariable();
		Value oldVal = env.get(variable);
		if(joinBlock != null) {
		    joinBlock.addPhi(variable, oldVal, phi);
		}
		env.put(variable, phi);
	    } else {
		// all phis will be at the begining, first non-phi
		// instruction we can break out of the loop
		break;
	    }
	}
    }

    // if a new Phi instruction was added, return it, else return null
    protected final Phi addPhiInternal(Identifier var, Value oldVal, Value newVal)
        throws Exception
    {
	// search for phi instruction for this variable
        for(Instruction i : instructions) {
            if(i instanceof Phi) {
                Phi p = (Phi)i;
                if(p.getVariable().equals(var)) {
                    // update the phi since there is already one for
                    // this variable, throw if the old value isn't
                    // part of the phi
                    p.updateArgument(oldVal, new VariableReference(var, newVal),
                                     currentBranch);
                    // we are now done, didn't add a new Phi
                    return null;
                }
            } else {
                // all phis must occur at the begining of the basic
                // block, if we get to an instruction that is not a
                // phi, we can stop our search
                break;
            }
        }
        // if we are here, we need to add a new phi. Wrap the new
        // value in VariableReference to the correct variable, the old
        // value will be a variable reference
        assert (oldVal instanceof VariableReference);
        Phi p = null;

        if(currentBranch == 1) {
            p = new Phi(var, new VariableReference(var, newVal), oldVal);
        } else if (currentBranch == 2) {
            p = new Phi(var, oldVal, new VariableReference(var, newVal));
        } else {
            throw new Exception("Invalid Phi position");
        }

        if(instructions.isEmpty()) {
            // if this is the first instruction, make it the most recent dominating
            p.setDominating(mostRecentDominating);
            mostRecentDominating = p;
        } else {
            // else add it into the linked list of dominating
            // instructions
            Instruction i = instructions.get(0);
            p.setDominating(i.getDominating());
            i.setDominating(p);
        }
        instructions.add(0, p);
        p.setContainingBB(this);
        // return the new Phi
        return p;
    }

    public void runPass(Pass p) throws Exception {
        if(p.requireBreadthFistTraversal()) {
            runBreadthFirstPass(p);
        } else {
            runDepthFirstPass(p);
        }
        currentPass++;
    }

    public void runBreadthFirstPass(Pass p) throws Exception {
        LinkedList<BasicBlock> blocks = new LinkedList<BasicBlock>();
        blocks.add(this);       // start on current BB
        while(!blocks.isEmpty()) {
            BasicBlock bb = blocks.pop();
            if(bb.localPass == currentPass) {
                continue;
            }
            bb.localPass = currentPass;
            // run pass on instructions in basic block
            ListIterator<Instruction> i = bb.instructions.listIterator(0);
            while(i.hasNext()) {
                p.run(i);
            }
            BasicBlock ch1 = bb.getFallThrough();
            BasicBlock ch2 = bb.getBranchTarget();
            if(ch1 != null) {
                blocks.add(ch1);
            }
            if(ch2 != null) {
                blocks.add(ch2);
            }
        }
    }

    public void runDepthFirstPass(Pass p) throws Exception {
        if(localPass == currentPass) {
            return;
        }
        localPass = currentPass;
        ListIterator<Instruction> i = instructions.listIterator(0);
        while(i.hasNext()) {
            p.run(i);
        }
        BasicBlock ch1 = getFallThrough();
        BasicBlock ch2 = getBranchTarget();
        if(ch1 != null) {
            ch1.runDepthFirstPass(p);
        }
        if(ch2 != null) {
            ch2.runDepthFirstPass(p);
        }
    }

    private HashSet<Value> calcLiveRangeInternal(BasicBlock branch, boolean secondTime, InterferenceGraph G)
        throws Exception {
        HashSet<Value> live;
        if(localLiveRangePass >= currentLiveRangePass) {
            live = new HashSet<Value>(this.live);
        } else {
            localLiveRangePass++;
            live = new HashSet<Value>();
            if(secondTime) {
                // go through all loop headers and add them to b.live
                // search through loop headers in breadth first order
                LinkedList<BasicBlock> blocks = new LinkedList<BasicBlock>();
                blocks.add(this);       // start on current BB
                while(!blocks.isEmpty()) {
                    BasicBlock bb = blocks.pop();
                    // need to change this to use a different pass
                    // number variable
                    if(bb.localPass == currentPass) {
                        continue;
                    }
                    bb.localPass = currentPass;
                    if(bb instanceof LoopHeader) {
                        this.live.addAll(bb.getLive());
                    }
                    BasicBlock ch1 = bb.getFallThrough();
                    BasicBlock ch2 = bb.getBranchTarget();
                    if(ch1 != null) {
                        blocks.add(ch1);
                    }
                    if(ch2 != null) {
                        blocks.add(ch2);
                    }
                }
                currentPass++;
            }

            BasicBlock ch1 = getFallThrough();
            BasicBlock ch2 = getBranchTarget();
            if(ch1 != null) {
                HashSet<Value> t = ch1.calcLiveRangeInternal(this, secondTime, G);
                live.addAll(t);
            }
            if(ch2 != null) {
                HashSet<Value> t = ch2.calcLiveRangeInternal(this, secondTime, G);
                live.addAll(t);
            }

            // for all non phi instructions do backwards
            Iterator<Instruction> iter = instructions.descendingIterator();
            while(iter.hasNext()) {
                Instruction i = iter.next();
                if(i instanceof Phi) {
                    break;
                }
                live.remove(i);
                if(i.needsRegister()) {
                    G.addNode(i);
                    for(Value v : live) {
                        G.addEdge(i, v);
                    }
                }
                ArrayList<Value> args = i.getArguments();
                for(int pos = 0; pos < args.size(); pos++) {
                    Value v = args.get(pos);
                    if(v.needsRegister()) {
                        live.add(v);
                    }
                    // because of the way I do cse the adda may not be
                    // right next to the load or store, and adda doesn't
                    // need a register, so we need to ensure that the adda
                    // arguments live until they are used in any load or
                    // store.
                    if(v instanceof Adda) {
                        args.addAll(((Adda)v).getArguments());
                    }
                }
            }
            // this.live = copy(live)
            this.live = new HashSet<Value>(live);
        }
        // for all phi instructions do backwards
        Iterator<Instruction> iter = instructions.descendingIterator();
        while(iter.hasNext()) {
            Instruction i = iter.next();
            if(i instanceof Phi) {
                Phi p = (Phi)i;
                live.remove(p);
                // phi instructoins will always need a register
                G.addNode(i);
                for(Value v : live) {
                    G.addEdge(p, v);
                }
                // add phi argument to live based on the branch number
                Value add = null;
                if(branch.equals(incomingBranch1)) {
                    add = p.getArg1();
                } else if (branch.equals(incomingBranch2)) {
                    add = p.getArg2();
                } else {
                    throw new Exception("Branch doesn't match");
                }
                if(add.needsRegister()) {
                    live.add(add);
                }
            }
        }

        return live;
    }

    public InterferenceGraph calcLiveRange() throws Exception {
        InterferenceGraph g = new InterferenceGraph();
        calcLiveRangeInternal(this, false, g);
        currentLiveRangePass++;
        calcLiveRangeInternal(this, true, g);
        currentLiveRangePass++;
        return g;
    }
}
