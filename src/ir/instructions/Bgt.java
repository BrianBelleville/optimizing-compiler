package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bgt extends BranchInstruction {

    public Bgt( BasicBlock target) {
        super(Opcode.bgt, target);
    }
}
