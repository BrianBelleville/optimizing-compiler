package transform;

import java.util.ListIterator;
import ir.base.Instruction;

public class InvalidateDominatorInformation implements Pass {
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        i.setDominating(null);
    }
}
