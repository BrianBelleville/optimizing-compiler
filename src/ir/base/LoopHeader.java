package ir.base;

import support.Identifier;
import ir.instructions.Phi;

// the difference between this and a normal BasicBlock is that this
// one will search through all of the instructions that it dominates
// when a phi is added to replace the variable used in the phi.
public class LoopHeader extends BasicBlock {
    public LoopHeader(BasicBlock dominator) {
	super(dominator);
    }

    @Override
    public void addPhi(Identifier var, Instruction oldVal, Instruction newVal) throws Exception
    {
	// add the Phi instruction in the same way
	Phi p = addPhiInternal(var, oldVal, newVal);
	// check if a new phi was added, and if so we need to search
	// and replace the old value with the Phi instruction
	if(p != null) {
	    // now perform depth first search through all instructions
	    // that are dominated by this block,
	    // only need to do this search if the phi was newly added
	    depthFirstReplace(this, oldVal, newVal);
	}
    }

    private static void depthFirstReplace(BasicBlock b, Instruction oldVal, Instruction newVal)
	throws Exception
    {
	for(Instruction i : b.instructions) {
	    i.replaceArgument(oldVal, newVal);
	}
	BasicBlock ch1 = b.getFallThrough();
	BasicBlock ch2 = b.getBranchTarget();
	if(ch1 != null) {
	    depthFirstReplace(ch1, oldVal, newVal);
	}
	if(ch2 != null) {
	    depthFirstReplace(ch2, oldVal, newVal);
	}
    }
}
