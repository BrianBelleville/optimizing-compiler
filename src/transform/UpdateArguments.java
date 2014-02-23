package transform;

import java.util.ListIterator;
import ir.base.Instruction;

public class UpdateArguments extends Pass {
    @Override
    public void run(ListIterator<Instruction> iter)
        throws Exception {
        Instruction i = iter.next();
        i.getArgumentSubstitutes();
    }
}
