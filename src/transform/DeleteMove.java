package transform;

import java.util.ListIterator;

import ir.Instruction;
import ir.Move;

public class DeleteMove extends Pass {
    @Override
    public void run(ListIterator<Instruction> iter)
        throws Exception {
        Instruction i = iter.next();
        if(i instanceof Move) {
            Move move = (Move)i;
            // delete moves for the value that they are moving
            move.delete(move.getArg());
            // the move instruction will still be in the dominator
            // tree, but will not interfere with CSE since all of the
            // CSE methods on Move are a nop
            iter.remove();
        }
    }
}
