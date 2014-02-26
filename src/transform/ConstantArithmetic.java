package transform;

import java.util.ListIterator;

import ir.ArithmeticInstruction;
import ir.Instruction;
import ir.Value;

public class ConstantArithmetic extends Pass {

    @Override
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        i.getArgumentSubstitutes();
        if(i instanceof ArithmeticInstruction) {
            ArithmeticInstruction a = (ArithmeticInstruction) i;
            if(a.isComputable()) {
                // if we can perform the computation at compile time,
                // do it and delete the instruction for the value it
                // would produce.
                Value v = a.performComputation();
                a.delete(v);
                // remove instruction if it has been computed at compile time
                iter.remove(); 
            }
        }
    }

    @Override
    public boolean requireBreadthFistTraversal() {
        return true;
    }
}
