package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bra extends BranchInstruction {

    public Bra(BasicBlock target) {
        super(Opcode.bra, target);
    }
}
