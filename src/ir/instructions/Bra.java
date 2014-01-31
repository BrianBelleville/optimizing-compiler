package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Bra extends BranchInstruction {

    public Bra(int num, BasicBlock containing, BasicBlock target) {
        super(num, containing, Opcode.bra, target);
    }
}
