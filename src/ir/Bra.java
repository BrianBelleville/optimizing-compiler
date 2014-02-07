package ir;

import ir.base.BranchInstruction;
import ir.base.Opcode;

public class Bra extends BranchInstruction {

    public Bra(BasicBlock target) {
        super(Opcode.bra, target);
    }
}
