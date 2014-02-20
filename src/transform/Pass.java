package transform;

import ir.base.Instruction;

public interface Pass {
    // this interface provides a pass that will be run on every
    // instruction in a basic block. Since the pass may alter the
    // structure of the instruction sequence, each call should return
    // the next instruction to run on. This allows all of the common
    // looping code to be eliminated from a pass.
    public abstract Instruction run(Instruction i);
}
