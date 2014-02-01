package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Ble extends BranchInstruction {

    public Ble( BasicBlock target) {
        super(Opcode.ble, target);
    }
}
