package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Beq extends BranchInstruction {

    public Beq(int num, BasicBlock containing, BasicBlock target) {
        super(num, containing, Opcode.beq, target);
    }
}
