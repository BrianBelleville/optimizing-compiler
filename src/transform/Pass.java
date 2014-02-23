package transform;

import java.util.ListIterator;
import ir.base.Instruction;


public abstract class Pass {
    // this interface provides a pass that will be run on every
    // instruction in a basic block. Since the pass may alter the
    // structure of the instruction sequence, we give the pass an
    // iterator. This can be used to modify the instruction sequence
    // as necessary. The method run will always be called with an iterator such
    // that i.hasNext() == true;
    public abstract void run(ListIterator<Instruction> i) throws Exception;

    public boolean requireBreadthFistTraversal() {
        return false;           // most passes don't require this
    }
}
