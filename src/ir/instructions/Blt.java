package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Blt extends BranchInstruction {

    public Blt(BasicBlock containing, BasicBlock target) {
        super(containing, Opcode.blt, target);
    }
}
