package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bgt extends BranchInstruction {

    public Bgt(int num, BasicBlock containing, BasicBlock target) {
        super(num, containing, Opcode.bgt, target);
    }
}
