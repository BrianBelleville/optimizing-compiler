package ir;

import ir.base.BranchInstruction;
import ir.base.Opcode;

public class Bne extends BranchInstruction {

    public Bne(BasicBlock target) {
        super(Opcode.bne, target);
    }
}
