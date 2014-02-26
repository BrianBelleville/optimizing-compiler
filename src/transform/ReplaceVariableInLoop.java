package transform;

import java.util.ListIterator;

import ir.Instruction;
import ir.Phi;
import ir.Value;
import ir.VariableReference;
import support.Identifier;

public class ReplaceVariableInLoop extends Pass {
    Phi phiInst;
    VariableReference newVal;
    Value oldVal;
    public ReplaceVariableInLoop(Identifier var, Phi phi, Value oldVal) {
        phiInst = phi;
        newVal = new VariableReference(var, phiInst);
        this.oldVal = oldVal;
    }

    @Override
    public void run(ListIterator<Instruction> iter)
        throws Exception {
        Instruction i = iter.next();
        // don't want to replace the actual phi instruction that
        // was just created
        if(i != phiInst) {
            i.replaceArgument(oldVal, newVal);
        }
    }
}
