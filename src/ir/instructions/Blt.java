package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Blt extends BranchInstruction {

    public Blt( BasicBlock target) {
        super(Opcode.blt, target);
    }
}
