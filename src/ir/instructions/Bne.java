package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bne extends BranchInstruction {

    public Bne(BasicBlock target) {
        super(Opcode.bne, target);
    }
}
