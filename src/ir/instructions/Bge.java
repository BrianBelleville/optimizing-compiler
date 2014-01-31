package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bge extends BranchInstruction {

    public Bge(BasicBlock containing, BasicBlock target) {
        super(containing, Opcode.bge, target);
    }
}
