package ir;

import java.util.ArrayList;

import support.Environment;
import support.Identifier;
import ir.base.BranchInstruction;
import ir.base.Instruction;
import ir.base.Value;
import java.io.IOException;
import java.io.Writer;

// todo: have a table containing the most recent instance of a
//       opcode, this can be used to perform CSE faster
public class BasicBlock {
    private BasicBlock dominator;
    public ArrayList<Instruction> instructions;
    private BasicBlock fallThrough;
    private static int blockNum = 0;
    private int number;
    private boolean printed = false;
    private Instruction mostRecentDominating;

    private int getNextBlockNum() {
        blockNum += 1;
        return blockNum;
    }

    public BasicBlock(BasicBlock dominator) {
        this.dominator = dominator;
        number = getNextBlockNum();
        instructions = new ArrayList<Instruction>();
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

    public void printBlock(Writer w) throws IOException {
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
        // even though i may not be added to the basic block, still
        // set which basic block it would be added to since it is used
        // during performCSE()
        i.setContainingBB(this);
        i.performCSE();
        if(!i.isDeleted()) {
            instructions.add(i);
            mostRecentDominating = i;
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
                    p.replaceArgument(oldVal, newVal, true);
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
        // if we are here, we need to add a new phi.
        Phi p = new Phi(var, oldVal, newVal);
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
}
