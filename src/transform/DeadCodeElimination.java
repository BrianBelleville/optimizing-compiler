package transform;

import java.util.ListIterator;
import ir.base.Instruction;
import ir.base.Value;
import ir.NamedValue;

public class DeadCodeElimination implements Pass {
    private static Value deleteFor = new NamedValue("DELETED");
    public void run(ListIterator<Instruction> iter)
        throws Exception {
        Instruction i = iter.next();
        // this may delete instructions, but they won't be removed
        // from the basic block at this point since it may delete
        // instructions outside of the basic block it is in. Deleted
        // instructions will need to be removed in a subsequent pass.
        performElimination(i);
    }
    private void performElimination(Instruction i) throws Exception {
        // if i is dead, but hasn't already been eliminated
        if(i.isDeadCode() && !i.isDeleted()) {
            i.delete(deleteFor);
            for(Value v : i.getArguments()) {
                v.removeUse(i);
                // v may now be dead code
                if(v instanceof Instruction) {
                    performElimination((Instruction)v);
                }
            }
        }
    }
}
