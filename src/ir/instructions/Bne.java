package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bne extends BranchInstruction {

    public Bne(BasicBlock containing, BasicBlock target) {
        super(containing, Opcode.bne, target);
    }
}
