package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bne extends BranchInstruction {

    public Bne(int num, BasicBlock containing, BasicBlock target) {
        super(num, containing, Opcode.bne, target);
    }
}
