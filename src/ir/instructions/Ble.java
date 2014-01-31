package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Ble extends BranchInstruction {

    public Ble(BasicBlock containing, BasicBlock target) {
        super(containing, Opcode.ble, target);
    }
}
