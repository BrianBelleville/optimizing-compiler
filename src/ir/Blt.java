package ir;

import ir.base.BranchInstruction;
import ir.base.Opcode;

public class Blt extends BranchInstruction {

    public Blt( BasicBlock target) {
        super(Opcode.blt, target);
    }
}
