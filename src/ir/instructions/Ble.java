package ir.instructions;

import ir.BasicBlock;
import ir.BranchInstruction;
import ir.Opcode;

public class Ble extends BranchInstruction {

    public Ble(int num, BasicBlock containing, BasicBlock target) {
        super(num, containing, Opcode.ble, target);
    }
}
