package ir;

import support.Identifier;
import ir.base.Instruction;
import ir.base.Value;

// the difference between this and a normal BasicBlock is that this
// one will search through all of the instructions that it dominates
// when a phi is added to replace the variable used in the phi.
public class LoopHeader extends BasicBlock {
    public LoopHeader(BasicBlock dominator) {
        super(dominator);
    }

    @Override
    public void addPhi(Identifier var, Value oldVal, Value newVal) throws Exception
    {
        // add the Phi instruction in the same way
        Phi p = addPhiInternal(var, oldVal, newVal);
        // check if a new phi was added, and if so we need to search
        // and replace the old value with the Phi instruction
        if(p != null) {
            // now perform depth first search through all instructions
            // that are dominated by this block,
            // only need to do this search if the phi was newly added
	    // replace the old values with the phi instruction
            depthFirstReplace(this, p, new VariableReference(var, p), oldVal, currentPass);
	    currentPass++;
        }
    }

    private static void depthFirstReplace(BasicBlock b, Phi phiInst, VariableReference newVal,
					  Value oldVal, int pass)
        throws Exception
    {
	if(b.localPass == pass) {
	    return;
	}
	b.localPass = pass;
        for(Instruction i : b.instructions) {
	    // don't want to replace the actual phi instruction that
	    // was just created
	    if(i != phiInst) {
		i.replaceArgument(oldVal, newVal);
	    }
        }
        BasicBlock ch1 = b.getFallThrough();
        BasicBlock ch2 = b.getBranchTarget();
        if(ch1 != null) {
            depthFirstReplace(ch1, phiInst, newVal, oldVal, pass);
        }
        if(ch2 != null) {
            depthFirstReplace(ch2, phiInst, newVal, oldVal, pass);
        }
    }
}
