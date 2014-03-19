package ir;

import transform.Pass;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import support.Environment;
import support.Identifier;
import support.InterferenceGraph;
import ir.Ret;
import ir.End;
import java.io.Writer;

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
            next.printBlockLayoutOrder(w);
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

    public void makeMovesForPhis() throws Exception {
        makeMovesForPhisInternal();
        currentPass++;
    }

    private void makeMovesForPhisInternal() throws Exception {
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

        insertTopoOrderedMoves(phis, 1);
        insertTopoOrderedMoves(phis, 2);

        BasicBlock ch1 = getFallThrough();
        BasicBlock ch2 = getBranchTarget();
        if(ch1 != null) {
            ch1.makeMovesForPhisInternal();
        }
        if(ch2 != null) {
            ch2.makeMovesForPhisInternal();
        }

    }

    private void insertTopoOrderedMoves(ArrayList<Phi> phis, int branchNum)
        throws Exception {
        BasicBlock incommingBlock = branchNum == 1 ? incomingBranch1 : incomingBranch2;
        HashMap<Integer, Phi> colorToNode = new HashMap<Integer, Phi>();
        HashMap<Phi, HashSet<Phi>> outGoingEdges = new HashMap<Phi, HashSet<Phi>>();
        HashMap<Phi, HashSet<Phi>> incommingEdges = new HashMap<Phi, HashSet<Phi>>();
        HashSet<Phi> noIncomming = new HashSet<Phi>(phis); // initially every node has no incomming edges
        // initialize data structures
        for(Phi p : phis) {
            colorToNode.put(p.getColor(), p);
            outGoingEdges.put(p, new HashSet<Phi>());
            incommingEdges.put(p, new HashSet<Phi>());
        }
        // create directed edges between conflicting moves
        for(Phi p : phis) {
            Value v = branchNum == 1 ? p.getArg1() : p.getArg2();
            Integer color = v.getColor();
            if(color == null) {
                continue;
            }

            Phi conflict = colorToNode.get(color);
            // don't make nodes conflict with themselves
            if(conflict == null || conflict == p) {
                continue;
            }

            noIncomming.remove(conflict);
            outGoingEdges.get(p).add(conflict);
            incommingEdges.get(conflict).add(p);
        }

        while(true) {
            // perform topological sorting and emit moves in that order
            while(!(noIncomming.isEmpty())) {
                // get an arbitrary element, we need to get a new iterator
                // each time through the loop since we may add to
                // noIncomming
                Phi p = noIncomming.iterator().next();
                if(!(incommingEdges.get(p).isEmpty())) {
                    throw new Exception("Sanity check failed");
                }
                noIncomming.remove(p);
                // remove edges from p to phis which must have moves after
                for(Phi after : outGoingEdges.get(p)) {
                    HashSet<Phi> incomming = incommingEdges.get(after);
                    incomming.remove(p);
                    if(incomming.isEmpty()) {
                        noIncomming.add(after);
                    }
                }
                outGoingEdges.remove(p);
                incommingEdges.remove(p);

                // emit the move for p
                Move m = new Move(branchNum == 1 ? p.getArg1() : p.getArg2());
                m.setColor(p.getColor());
                incommingBlock.addAtEndBeforeBranch(m);
            }
            if((outGoingEdges.isEmpty() && incommingEdges.isEmpty())) {
                return;         // we are done, graph is empty
            } else {
                // Else there is a cycle, we need to insert anonymous
                // temporaries. Each cycle should be able to be broken
                // with a single temporary, and once broken we will be
                // able to insert moves again. If we encounter a
                // second cycle, the first should have already been
                // resoved, so we can then reuse that temporary.
                
                // get a node and its argument
                Entry<Phi, HashSet<Phi>>  e = outGoingEdges.entrySet().iterator().next();
                Phi p = e.getKey();
                HashSet<Phi> outGoing = e.getValue();

                // insert the move here
                Value arg = branchNum == 1 ? p.getArg1() : p.getArg2();
                Move m = new Move(arg);
                // move to temporary. This will be assigned to a register during code generation
                m.setColor(-2);
                incommingBlock.addAtEndBeforeBranch(m);
                p.updateArgument(arg, new Temporary(), branchNum);

                // remove outgoing edges of p, its arg is now safe in
                // a temporary, so there is no longer a dependance on
                // the other moves coming after
                for(Phi after : outGoing) {
                    HashSet<Phi> incomming = incommingEdges.get(after);
                    incomming.remove(p);
                    if(incomming.isEmpty()) {
                        noIncomming.add(after);
                    }
                }
                outGoing.clear();

                // if we didn't make a node with no incoming edges, we're screwed
                if(noIncomming.isEmpty()) {
                    throw new Exception("Phi cycle not resolved by a single temporary");
                }
            }
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

    private HashSet<Value> calcLiveRangeInternal(BasicBlock branch, boolean secondTime, InterferenceGraph G, LinkedList<LoopHeader> enclosingHeaders)
        throws Exception {
        HashSet<Value> live;
        if(localLiveRangePass >= currentLiveRangePass) {
            live = new HashSet<Value>(this.live);
        } else {
            localLiveRangePass++;
            live = new HashSet<Value>();
            if(secondTime) {
                // go through all enclosing loop headers and add them to b.live
                // search through loop headers in breadth first order
                for(LoopHeader he : enclosingHeaders) {
                    this.live.addAll(he.getLive());
                }
            }

            BasicBlock ch1 = getFallThrough();
            BasicBlock ch2 = getBranchTarget();
            if(ch1 != null) {
                // if this is a loop header, it will enclose all basic blocks folowing its fall through
                if(this instanceof LoopHeader) {
                    enclosingHeaders.push((LoopHeader)this);
                }
                HashSet<Value> t = ch1.calcLiveRangeInternal(this, secondTime, G, enclosingHeaders);
                if(this instanceof LoopHeader) {
                    enclosingHeaders.pop();
                }
                live.addAll(t);
            }
            if(ch2 != null) {
                HashSet<Value> t = ch2.calcLiveRangeInternal(this, secondTime, G, enclosingHeaders);
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



        ArrayList<Instruction> phis = new ArrayList<Instruction>();
        Iterator<Instruction> iter = instructions.descendingIterator();
        while(iter.hasNext()) {
            Instruction i = iter.next();
            if(i instanceof Phi) {
                phis.add(i);
            }
        }


        // for all phi instructions do backwards
        // (phis will contain the phi instructions in backwards order
        //  already)
        for(Instruction i : phis) {
            Phi p = (Phi)i;
            live.remove(p);
            // phi instructions will always need a register
            G.addNode(i);
            for(Value v : live) {
                G.addEdge(p, v);
            }
            // add phi argument to live based on the branch number if
            // we can resolve cycles in phi moves, adding the
            // arguments at this point shouldn't be stricktly
            // necessary, but it looks like it can actually lead to
            // worse code, you'll use less registers during the
            // computation, but will require additional instructions
            // for moves.
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

        // add the phi arguments to live since the semantics of phi
        // instructions is that they all happen in parallel, and if
        // the arg of one phi is another phi (can happen when there is
        // a backwards branch in a loop), that arg will have been
        // removed from the live range, but it is imperative that the
        // arguments are all live when this block is entered.
        for(Instruction i : phis) {
            Phi p = (Phi)i;
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

        return live;
    }

    public InterferenceGraph calcLiveRange() throws Exception {
        InterferenceGraph g = new InterferenceGraph();
        LinkedList<LoopHeader> enclosingHeaders = new LinkedList<LoopHeader>();
        calcLiveRangeInternal(this, false, g, enclosingHeaders);
        currentLiveRangePass++;
        calcLiveRangeInternal(this, true, g, enclosingHeaders);
        currentLiveRangePass++;
        return g;
    }
}
