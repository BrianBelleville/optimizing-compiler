package transform;

import java.util.ListIterator;

import ir.Instruction;

public class RemoveDeletedInstructions extends Pass {
    @Override
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        if(i.isDeleted()) {
            iter.remove();
        }
    }
}
