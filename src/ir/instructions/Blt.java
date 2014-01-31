package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Blt extends BranchInstruction {

    public Blt(int num, BasicBlock containing, BasicBlock target) {
        super(num, containing, Opcode.blt, target);
    }
}
