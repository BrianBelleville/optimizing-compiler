package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bge extends BranchInstruction {

    public Bge(int num, BasicBlock containing, BasicBlock target) {
        super(num, containing, Opcode.bge, target);
    }
}
