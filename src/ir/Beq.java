package ir;

import ir.base.BranchInstruction;
import ir.base.Opcode;

public class Beq extends BranchInstruction {

    public Beq(BasicBlock target) {
        super(Opcode.beq, target);
    }
}
