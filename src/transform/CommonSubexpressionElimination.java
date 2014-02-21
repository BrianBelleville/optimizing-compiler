package transform;

import java.util.ListIterator;
import ir.base.Instruction;

public class CommonSubexpressionElimination implements Pass {
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        i.performCommonSubexpressionElimination();
        if(i.isDeleted()) {
            iter.remove();
        }
    }
}
