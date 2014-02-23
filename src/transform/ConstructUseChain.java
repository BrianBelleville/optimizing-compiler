package transform;

import java.util.ListIterator;
import java.util.ArrayList;
import ir.base.Value;
import ir.base.Instruction;

public class ConstructUseChain extends Pass {
    @Override
    public void run(ListIterator<Instruction> iter)
        throws Exception {
        Instruction i = iter.next();
        ArrayList<Value> args = i.getArguments();
        for(Value v : args) {
            v.addUse(i);
        }
    }
}
