package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bge extends BranchInstruction {

    public Bge(BasicBlock target) {
        super(Opcode.bge, target);
    }
}
