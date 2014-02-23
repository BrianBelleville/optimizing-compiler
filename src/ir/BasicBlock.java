package ir;

import transform.Pass;
import java.util.LinkedList;
import java.util.ListIterator;
import support.Environment;
import support.Identifier;
import ir.base.BranchInstruction;
import ir.base.Instruction;
import ir.base.Value;
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
    private boolean printed = false;
    private Instruction mostRecentDominating;

    private int getNextBlockNum() {
        blockNum += 1;
        return blockNum;
    }

    public BasicBlock(BasicBlock dominator) {
        this.dominator = dominator;
        number = getNextBlockNum();
        instructions = new LinkedList<Instruction>();
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
                    p.replaceArgument(oldVal, new VariableReference(var, newVal), true);
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
        assert oldVal instanceof VariableReference;
        Phi p = new Phi(var, oldVal, new VariableReference(var, newVal));
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

    public void stripVarRefs() throws Exception {
        stripVarRefsInternal();
        BasicBlock.currentPass++;
    }

    public void runPass(Pass p) throws Exception {
        if(p.requireBreadthFistTraversal()) {
            runBreadthFirstPass(p);
            currentPass++;
        }
        runDepthFirstPass(p);
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

    private void stripVarRefsInternal() throws Exception {
        if(localPass == currentPass) {
            return;
        }
        localPass = BasicBlock.currentPass;
        for(Instruction i : instructions) {
            i.removeVariableReferenceArguments();
        }
        BasicBlock ch1 = getFallThrough();
        BasicBlock ch2 = getBranchTarget();
        if(ch1 != null) {
            ch1.stripVarRefsInternal();
        }
        if(ch2 != null) {
            ch2.stripVarRefsInternal();
        }
    }
}
