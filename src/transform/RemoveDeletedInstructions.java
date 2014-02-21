package transform;

import java.util.ListIterator;
import ir.base.Instruction;

public class RemoveDeletedInstructions implements Pass {
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        if(i.isDeleted()) {
            iter.remove();
        }
    }
}
