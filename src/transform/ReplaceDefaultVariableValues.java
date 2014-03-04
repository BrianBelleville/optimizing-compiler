package transform;

import java.util.ListIterator;
import ir.Instruction;
import ir.Value;
import ir.DefaultValue;
import ir.Immediate;

public class ReplaceDefaultVariableValues extends Pass {
    @Override
    public void run(ListIterator<Instruction> iter)
        throws Exception {
        Instruction i = iter.next();
        for(Value v : i.getArguments()) {
            if(v instanceof DefaultValue) {
                i.replaceArgument(v, new Immediate(0));
            }
        }
    }
}
