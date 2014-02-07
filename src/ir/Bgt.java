package ir;

import ir.base.BranchInstruction;
import ir.base.Opcode;

public class Bgt extends BranchInstruction {

    public Bgt( BasicBlock target) {
        super(Opcode.bgt, target);
    }
}
