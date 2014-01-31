package ir;

import java.util.ArrayList;
import support.Identifier;
import ir.instructions.Phi;
// todo: have a table containing the most recent instance of a
//       opcode, this can be used to perform CSE faster
public class BasicBlock {
    private BasicBlock dominator;
    public ArrayList<Instruction> instructions;
    private BasicBlock fallThrough;
    private static int blockNum = 0;
    private int number;

    private int getNextBlockNum() {
	blockNum += 1;
	return blockNum;
    }

    public BasicBlock(BasicBlock dominator) {
	this.dominator = dominator;
	number = getNextBlockNum();
    }

    public BasicBlock getFallThrough() {
        return fallThrough;
    }
    public BasicBlock getBranchTarget() {
        // todo: get the branch target of the last instruction
        // a branch instruction will always end a basic block,
        // but not every basic block must end with a branch
        return null;
    }
    public void emit(Instruction i) {
        instructions.add(i);
        i.performCSE();
    }

    public void addPhi(Identifier var, Instruction oldVal, Instruction newVal) throws Exception
    {
	for(Instruction i : instructions) {
	    if(i instanceof Phi) {
		Phi p = (Phi)i;
		if(p.getVariable() == var) {
		    // update the phi since there is already one for this variable
		    p.replaceArgument(oldVal, newVal);
		    // we are now done
		    return;
		}
	    } else {
		// all phis must occur at the begining of the basic
		// block, if we get to an instruction that is not a
		// phi, we can stop our search
		break;
	    }
	}
	// if we are here, we need to add a new phi.
	Phi p = new Phi(this, var, oldVal, newVal);
	instructions.add(0, p);
    }
}
