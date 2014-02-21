package ir;

import support.Identifier;
import ir.base.Value;
import transform.ReplaceVariableInLoop;

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
            runPass(new ReplaceVariableInLoop(var, p, oldVal));
        }
    }
}
