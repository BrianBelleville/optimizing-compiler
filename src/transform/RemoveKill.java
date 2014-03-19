package transform;

import java.util.ListIterator;

import ir.Instruction;
import ir.Kill;

public class RemoveKill extends Pass {
    @Override
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        if(i instanceof Kill) {
            iter.remove();
        }
    }
}
