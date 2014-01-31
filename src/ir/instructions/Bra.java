package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bra extends BranchInstruction {

    public Bra(BasicBlock containing, BasicBlock target) {
        super(containing, Opcode.bra, target);
    }
}
