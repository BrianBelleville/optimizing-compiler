package transform;

import java.util.ListIterator;

import ir.Instruction;

public class CommonSubexpressionElimination extends Pass {
    @Override
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        i.getArgumentSubstitutes();
        i.performCSE();
        if(i.isDeleted()) {
            iter.remove();
        }
    }
}
