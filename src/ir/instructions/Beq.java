package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Beq extends BranchInstruction {

    public Beq(BasicBlock containing, BasicBlock target) {
        super(containing, Opcode.beq, target);
    }
}
